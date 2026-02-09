package Production.AuthService.controllers;

import Production.AuthService.SecurityUtils.CookieService;
import Production.AuthService.config.SecurityConfig;
import Production.AuthService.SecurityUtils.JwtService;
import Production.AuthService.dtos.LoginRequestDto;
import Production.AuthService.dtos.TokenResponseDto;
import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.entities.RefreshToken;
import Production.AuthService.entities.User;
import Production.AuthService.exceptions.InvalidResourceFoundException;
import Production.AuthService.repositories.RefreshTokenRepository;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
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
                bearer(accessToken,refreshToken, jwtService.getAccessTTL(),"Bearer",modelMapper.map(user,UserResponseDto.class));

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
}
