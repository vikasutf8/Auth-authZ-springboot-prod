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

user --->use some react/browser  --> React connect Backend[SpringBoot]

Client  === Browser(react)+ Backend(Springboot)  NOTE: react we dont use(backend provide token to it)
Google/github === auth provider used as like of auth server(auth server >>>> auth provider)
Resource Server === eg: spring boot written apis -NOTE: not done here...we create our backend server + resource server make it one [client backend  ~~ Resource Server]

authorized redirect url:
```shell
${api_domain : http://localhost}:${PORT}/login/oauth2/code/google
```




