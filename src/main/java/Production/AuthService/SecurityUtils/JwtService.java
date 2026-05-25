package Production.AuthService.SecurityUtils;


import Production.AuthService.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Getter
@Setter
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTTL;
    private final long refreshTTL;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTTL,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTTL,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        if(secret ==null || secret.length()<64){
            throw new IllegalArgumentException("Invalid Secret");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTTL = accessTTL;
        this.refreshTTL = refreshTTL;
        this.issuer = issuer;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode("flajksdfhaksjdfh"));
    }
// ── Generate ──────────────────────────────────────────────

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", user.getRoles())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTTL * 1000))
                .signWith(signingKey())
                .compact();
    }

    public String generateRefreshToken(User user, String jti) {
        return Jwts.builder()
                .subject(user.getEmail())
                .id(jti)                     // jti links JWT → DB row for revocation
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTTL * 1000))
                .signWith(signingKey())
                .compact();
    }

    // ── Extract ───────────────────────────────────────────────

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);          // throws if expired or tampered
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTTL() {
        return accessTTL;
    }

    public long getRefreshTTL() {
        return refreshTTL;
    }
//    //generate token -:generateAccessToken(User user)
//    /*
//    {
//  "iss": "api.substring.com",
//  "sub": "admin@example.com",
//  "uid": "a2be2538-2696-49d9-b334-2a9348efa93e",
//  "roles": ["ROLE_ADMIN", "ROLE_USER"],
//  "iat": 1707316220,
//  "exp": 1707319820
//}
//
//     */
//    public String generateAccessToken(User user) {
//
//        Instant now = Instant.now();
//        Instant expiry = now.plusSeconds(accessTTL);
//
//        List<String> roles = user.getRoles()
//                .stream()
//                .map(role -> "ROLE_" + role.getName())
//                .toList();
//
//        return Jwts.builder()
//                .issuer(issuer)
//                .id(UUID.randomUUID().toString())
//                .subject(user.getId().toString())     // primary identity
//                .claim("email", user.getEmail())
//                .claim("roles", roles)
//                .claim("tpe","access")
//                .issuedAt(Date.from(now))
//                .expiration(Date.from(expiry))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    //generateRefereshToken
//    public String generateRefreshToken(User user,String jti) {
//
//        Instant now = Instant.now();
//        Instant expiry = now.plusSeconds(refreshTTL);
//
////        List<String> roles = user.getRoles()
////                .stream()
////                .map(Role::getName)   // or role.getRoleName()
////                .collect(Collectors.toList());
//        List<String> roles = user.getRoles()
//                .stream()
//                .map(role -> "ROLE_" + role.getName())
//                .toList();
//
//        return Jwts.builder()
//                .issuer(issuer)
//                .id(jti)
//                .subject(user.getId().toString())     // primary identity
//                .claim("email", user.getEmail())
//                .claim("roles", roles)
//                .claim("tpe","refresh")
//                .issuedAt(Date.from(now))
//                .expiration(Date.from(expiry))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    //parse the token
//    public Claims parseToken(String token) {
//        try {
//            return Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//        } catch (JwtException | IllegalArgumentException ex) {
//            throw new InvalidResourceFoundException("Invalid token");
//        }
//    }
//
//
//    //validate token
//    public Claims validateAccessToken(String token) {
//        Claims claims = parseToken(token);
//
//        String type = claims.get("tpe", String.class);
//        if (!"access".equals(type)) {
//            throw new InvalidResourceFoundException("Invalid access-token type");
//        }
//
//        return claims;
//    }
//
//    public Claims validateRefreshToken(String token) {
//        Claims claims = parseToken(token);
//
//        String type = claims.get("tpe", String.class);
//        if (!"refresh".equals(type)) {
//            throw new InvalidResourceFoundException("Invalid refresh-token type");
//        }
//
//        return claims;
//    }
//
//    public UUID extractUserId(String token) {
//        Claims claims = validateAccessToken(token);
//        return UUID.fromString(claims.getSubject());
//    }
//    public UUID extractUserIdFromRefreshToken(String token) {
//        Claims claims = validateRefreshToken(token);
//        return UUID.fromString(claims.getSubject());
//    }
//
//    public String extractRefreshTokenId(String token){
//        Claims claims = validateRefreshToken(token);
//        return  claims.getId();
//    }
//
//    public String extractEmail(String token) {
//        return validateAccessToken(token).get("email", String.class);
//    }
//
////    public List<String> extractRoles(String token) {
////        return validateAccessToken(token).get("roles", List.class);
////    }
//    public List extractRoles(String token) {
//        return validateAccessToken(token).get("roles", List.class)
//                .stream()
//                .map(Object::toString)
//                .toList();
//}
}
