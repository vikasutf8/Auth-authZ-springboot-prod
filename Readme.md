# Auth-Prod

- step1 : backend project setup

- step2:  frontend at different origin so CORS

- adding Jwt filters

- Oauth concept -pending

-  Security  : access_token and refresh_token both only access by one user only  / self only

# API Documation : Swagger 
`http://localhost:{9081==PORT}/swagger-ui/index.html#/`

### Postman 
`https://crimson-comet-847628.postman.co/workspace/springBoot-fitness~3624bf6f-c3b5-4845-b04e-6d7f7fcd62fc/collection/25455646-cd70689a-dba5-41cc-8c5e-2caa1c08674c?action=share&creator=25455646`


[TODO] Oauth with google/github server----------do it rightnow
### Oauth -- Only Treat on backend --[Backend As a Service]
# 🔐 OAuth2 Login with Google & GitHub
## Backend as a Service (Spring Boot Only)

---

## 📌 Overview

This project implements OAuth2 authentication using **Google** and **GitHub** in a Spring Boot backend.

It follows a **Backend-as-a-Service (BaaS)** pattern:

- No frontend token handling logic
- Backend manages OAuth flow
- Backend generates JWT tokens
- Backend acts as:
    - OAuth2 Client
    - Internal JWT Authorization Server
    - Resource Server (secured APIs)

---

# 🏗 Architecture

User  
↓  
Browser (React or any client)  
↓  
Spring Boot Backend  
↓  
Google / GitHub (OAuth Provider)

---

## 🎯 Roles Explained

| Component | Role |
|------------|--------|
| Browser / React | Triggers login |
| Spring Boot | OAuth Client + JWT Issuer + Resource Server |
| Google / GitHub | External Authentication Provider |
| JWT | Secures APIs |

---

## /login ==> JWT

```shell
1. SecurityConfig          — stateless, permit /auth/**, filter chain
2. JwtService              — generate / validate / extract claims
3. JwtAuthFilter           — OncePerRequestFilter, reads Bearer token
4. UserDetailsServiceImpl  — loadUserByUsername via email
5. AuthService             — authenticate, persist refresh, generate tokens
6. AuthController          — POST /api/v1/auth/login
```

#### sequence

```shell
Client → POST /login (email, password)
  → AuthController
    → AuthService.login()
      → AuthenticationManager.authenticate()         [1. verify credentials]
        → UserDetailsServiceImpl.loadUserByUsername() [2. load from DB]
      → RefreshTokenRepository.save()                [3. persist jti to DB]
      → JwtService.generateAccessToken()             [4. sign access JWT]
      → JwtService.generateRefreshToken(jti)         [5. sign refresh JWT]
      → CookieService.attachRefreshTokenCookie()     [6. HttpOnly cookie]
  ← LoginResponse(accessToken, expiresIn, "Bearer")

Every subsequent request:
Client → GET /any (Authorization: Bearer <token>)
  → JwtAuthFilter
    → JwtService.extractEmail()
    → UserDetailsServiceImpl.loadUserByUsername()
    → SecurityContextHolder.setAuthentication()
  → Controller (authenticated)
```

# 🔁 OAuth Flow (Backend Only)

```shell
1. SecurityConfig          — add oauth2Login, successHandler, failureHandler
2. OAuth2UserInfo          — abstract, normalize Google/GitHub provider data
3. GoogleOAuth2UserInfo    — extract fields from Google attributes
4. GithubOAuth2UserInfo    — extract fields from GitHub attributes
5. CustomOAuth2UserService — loadUser, upsert User in DB
6. OAuth2SuccessHandler    — generate JWT, attach cookie, redirect
7. OAuth2FailureHandler    — redirect with error param
```

#### sequence

```shell
Client → GET /oauth2/authorize/google
  → Spring redirects to Google consent screen

Google → GET /login/oauth2/code/google?code=xxx
  → CustomOAuth2UserService.loadUser()
    → fetch attributes from Google
    → OAuth2UserInfoFactory.getOAuth2UserInfo()   [normalize provider data]
    → userRepository.findByEmail()
      → EXISTS  → update name/image                [returning user]
      → NOT EXISTS → create new User               [first time OAuth]
  → OAuth2SuccessHandler.onAuthenticationSuccess()
    → RefreshTokenRepository.save(jti)
    → JwtService.generateAccessToken()
    → JwtService.generateRefreshToken(jti)
    → CookieService.attachRefreshTokenCookie()
    → redirect → frontend/dashboard?accessToken=xxx

Client (subsequent requests same as JWT flow)
  → JwtAuthFilter → SecurityContext → Controller
```

## OIDC vs OAuth2

```shell
OAuth2   → "Can I access your Google data?"     → gives you ACCESS TOKEN
OIDC     → "Who are you?"                        → gives you ID TOKEN (JWT with user claims)

OAuth2UserService  → calls /userinfo endpoint separately to get user data
OidcUserService    → ID token already HAS user claims (sub, email, name, picture)
                     no extra HTTP call needed → faster + standard
```

#### What changes from your current OAuth2 setup

```shell
CustomOAuth2UserService   →  CustomOidcUserService
  extends DefaultOAuth2UserService  →  extends OidcUserService
  OAuth2User return type            →  OidcUser return type
  manual attribute extraction       →  standardClaims() built-in

SecurityConfig
  .userService(customOAuth2UserService)   →  .oidcUserService(customOidcUserService)

application.yml
  scope: email, profile   →  scope: openid, email, profile   ← openid triggers OIDC
```

#### sequence

```shell
Client → GET /oauth2/authorize/google
  → Spring appends scope=openid → triggers OIDC flow

Google → returns ID TOKEN (JWT) + optional access token
  → Spring validates ID token signature (via Google JWKS endpoint)
  → Spring calls CustomOidcUserService.loadUser()
    → OidcUserInfo has standardClaims() — no extra /userinfo call
    → upsert User in DB same as before
  → OAuth2SuccessHandler (unchanged)
    → generate JWT, attach cookie, redirect
```

#### OAuth2 vs OIDC side by side

```shell
OAuth2 (GitHub)              OIDC (Google)
─────────────────────────────────────────────────────────────────
scope               user:email, read:user        openid, email, profile
token returned      access token only            ID token (JWT) + access token
user data           extra /userinfo HTTP call     already in ID token claims
signature verify    no                           yes — Spring checks Google JWKS
your service        CustomOAuth2UserService      CustomOidcUserService
principal type      OAuth2User                   OidcUser
email extracted     attributes.get("email")      oidcUser.getEmail()
sub claim           attributes.get("id")         oidcUser.getSubject()
```

### APIs

```shell
POST /api/v1/auth/register   →  email/password → JWT
POST /api/v1/auth/login      →  email/password → JWT
GET  /oauth2/authorize/google →  OIDC  → ID token validated → JWT
GET  /oauth2/authorize/github →  OAuth2 → /userinfo call   → JWT

All three end up at OAuth2SuccessHandler or AuthService
→ same RefreshToken table
→ same JwtService
→ same HttpOnly cookie + accessToken response
```

1. User clicks **Login with Google/GitHub**
2. Browser redirects to:

   ```
   /oauth2/authorization/google
   ```

3. Spring Boot redirects to Google/GitHub
4. User authenticates
5. Provider redirects to:

   ```
   ${api_domain:http://localhost}:${PORT}/login/oauth2/code/google
   ```

6. Spring Boot:
    - Receives authorization code
    - Exchanges code for access token
    - Fetches user info
    - Creates or updates user in DB
    - Generates:
        - JWT Access Token
        - JWT Refresh Token
    - Stores refresh token in DB
    - Sends:
        - Access token in redirect URL
        - Refresh token as HttpOnly cookie

---

# 🔑 Authorized Redirect URLs

Configure in Google/GitHub console:

```
http://localhost:9081/login/oauth2/code/google
http://localhost:9081/login/oauth2/code/github
```

---

# 🧠 System Design

## Backend Acts As:

### 1️⃣ OAuth2 Client

Configured using:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
```

---

### 2️⃣ Authorization Server (Internal JWT Issuer)

After successful OAuth login:

- Generates Access Token
- Generates Refresh Token
- Stores refresh token in database

---

### 3️⃣ Resource Server

All secured APIs validate:

- JWT Access Token
- Token expiry
- User roles

---

# 🔐 Token Strategy

| Token | Storage | TTL |
|--------|----------|------|
| Access Token | Frontend (memory) | Short-lived |
| Refresh Token | HttpOnly Cookie + DB | Long-lived |

---

# 🗂 Database Tables

### users
- id
- email
- name
- provider
- providerId
- roles

### refresh_tokens
- id
- jti
- user_id
- revoked
- expires_at

---

# 🚀 Login Endpoints

| Provider | Endpoint |
|----------|------------|
| Google | `/oauth2/authorization/google` |
| GitHub | `/oauth2/authorization/github` |

---

# 📦 Required Dependency

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

---

# 🔒 Production Notes

- Set `cookie-secure: true`
- Use HTTPS
- Rotate JWT secret
- Implement refresh token revocation
- Add role-based authorization
- Enable correlation ID logging

---

# 🎯 Final Summary

✔ Uses Google & GitHub as authentication providers  
✔ Issues its own JWT tokens  
✔ Stores refresh tokens securely  
✔ Acts as OAuth client and resource server  
✔ No frontend token logic required

---




