package Production.AuthService.controllers;

import Production.AuthService.SecurityUtils.CookieService;
import Production.AuthService.config.SecurityConfig;
import Production.AuthService.SecurityUtils.JwtService;
import Production.AuthService.dtos.*;
import Production.AuthService.entities.RefreshToken;
import Production.AuthService.entities.User;
import Production.AuthService.exceptions.InvalidResourceFoundException;
import Production.AuthService.repositories.RefreshTokenRepository;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v3/auth")
public class AuthController {
    private final RefreshTokenRepository refreshTokenRepository;
    private  final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response){

        log.info(loginRequestDto.username(),loginRequestDto.password());
        //authenticate
        Authentication authentication =authenticate(loginRequestDto);
        User user = userRepository.findByEmail(loginRequestDto.username()).
                orElseThrow(()-> new InvalidResourceFoundException("Invalid password and Email !!"));
        //check taht user enable --twice check
        if(!user.isEnable()){
            throw new DisabledException("DeActive User, Please connect admin");
        }

        // gerneate refresh token
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokendb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTTL()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokendb);


        //generate token
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user,refreshTokendb.getJti()); //its db val

        //attach cookies
        cookieService.attachRefreshTokenCookie(response,refreshToken,(int)jwtService.getRefreshTTL());
        cookieService.addNoStoreHeaders(response);


        TokenResponseDto tokenResponseDto =TokenResponseDto.
                bearer(accessToken,refreshToken, jwtService.getAccessTTL(),"refresh",modelMapper.map(user,UserResponseDto.class));

        return  ResponseEntity.ok(tokenResponseDto);
    }

    private Authentication authenticate(@Valid LoginRequestDto loginRequestDto) {
        try{
            return authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequestDto.username(),loginRequestDto.password()));

        } catch (Exception e) {
            throw new InvalidResourceFoundException("Invalid password and Email !!");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.registerUser(userRequestDto));
//        return null;
    }

    //refresh-token /access-token renew
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
            HttpServletResponse response,
            HttpServletRequest request
    ){
        //read token from cookie

        String refreshTokenfromRequest =readRefreshTokenFromRequest(refreshTokenRequest,request).orElseThrow(()->new InvalidResourceFoundException("Refresh token missing"));
        if(jwtService.validateRefreshToken(refreshTokenfromRequest).isEmpty()){
            throw new InvalidResourceFoundException("Invalid Cred;");
        }

        String jti = jwtService.extractRefreshTokenId(refreshTokenfromRequest);
        UUID userId =jwtService.extractUserIdFromRefreshToken(refreshTokenfromRequest);
        RefreshToken storedRefreshToken =refreshTokenRepository.findByJti(jti).orElseThrow(()-> new InvalidResourceFoundException("Invalid Cred;"));

        if(storedRefreshToken.isRevoked()){
            throw new InvalidResourceFoundException("Revoked Already");
        }

        if(storedRefreshToken.getExpiresAt().isBefore(Instant.now())){
            throw new InvalidResourceFoundException("Token expired");
        }

//        if(storedRefreshToken.getUser().getId().equals(userId)){
//            throw new InvalidResourceFoundException("Invalid User");
//        }

        //refresh token rotate
        storedRefreshToken.setRevoked(true);
        String newJti =UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user= storedRefreshToken.getUser();

        //new refresh token
        var newRefreshTokenObj =RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTTL()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenObj);
        String newAccessToken =jwtService.generateAccessToken(user);
        String newRefreshToken =jwtService.generateRefreshToken(user, newRefreshTokenObj.getJti());

        cookieService.attachRefreshTokenCookie(response,newRefreshToken, (int) jwtService.getRefreshTTL());
        cookieService.addNoStoreHeaders(response);

        return ResponseEntity.ok(
                TokenResponseDto
                        .bearer(newAccessToken,newRefreshToken,
                                jwtService.getAccessTTL(),"Bearer",modelMapper.map(user,UserResponseDto.class)));


    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        readRefreshTokenFromRequest(null, request)
                .ifPresent(token -> {
                    try {
                        if (jwtService.validateRefreshToken(token).isEmpty()) {
                            return;
                        }

                        String jti = jwtService.extractRefreshTokenId(token);

                        refreshTokenRepository.findByJti(jti)
                                .ifPresent(rt -> {
                                    rt.setRevoked(true);
                                    refreshTokenRepository.save(rt);
                                });

                    } catch (JwtException ignored) {
                        // intentionally ignored – logout should always succeed
                    }
                });

        // Clear cookie
        cookieService.clearRefreshTokenCookie(response);

        // No-store headers
        cookieService.addNoStoreHeaders(response);

        // Clear security context
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    private Optional<String> readRefreshTokenFromRequest(
            RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request
    ) {

        // 1️⃣ Cookie (preferred)
        if (request.getCookies() != null) {
//            for (Cookie cookie : request.getCookies()) {
//                if ("refresh_token".equals(cookie.getName())
//                        && cookie.getValue() != null
//                        && !cookie.getValue().isBlank()) {
//
//                    return Optional.of(cookie.getValue());
//                }
//            }

             Optional<String> fromCookies= Arrays.stream(request.getCookies()).filter(
                    c ->cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v ->!v.isBlank()).findFirst();

             if(fromCookies.isPresent()){
                 return fromCookies;
             }

        }

        // 2️⃣ Request body
        if (refreshTokenRequest != null
                && refreshTokenRequest.refreshToken() != null
                && !refreshTokenRequest.refreshToken().isBlank()) {

            return Optional.of(refreshTokenRequest.refreshToken());
        }

        // 3️⃣ Custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader);
        }

        // 4️⃣ Authorization header (Bearer <token>)
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String cred =authHeader.substring(7);
            if(!cred.isEmpty()){
                try{
                    if(!jwtService.validateRefreshToken(cred).isEmpty()){
                        return Optional.of(cred);
                    }
                }catch (Exception ignored){}
            }
        }

        return Optional.empty();
    }

}
