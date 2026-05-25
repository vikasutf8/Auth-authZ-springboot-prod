package Production.AuthService.services.implement;

import Production.AuthService.SecurityUtils.CookieService;
import Production.AuthService.SecurityUtils.JwtService;
import Production.AuthService.dtos.Request.LoginRequest;
import Production.AuthService.dtos.Request.RegisterRequest;
import Production.AuthService.dtos.Response.LoginResponse;
import Production.AuthService.dtos.Response.RegisterResponse;
import Production.AuthService.entities.RefreshToken;
import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import Production.AuthService.exceptions.EmailAlreadyExistsException;
import Production.AuthService.exceptions.InvalidResourceFoundException;
import Production.AuthService.repositories.RefreshTokenRepository;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.AuthService;
import Production.AuthService.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImple implements AuthService {

    private  final UserService userService;
    private  final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;
    private final JwtService jwtService;
//    @Override
//    public Re registerUser(UserRequestDto userRequestDto) {
//
//        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
//        return userService.createUser(userRequestDto);
//    }

    @Override
    public RegisterResponse registerUser(RegisterRequest userRequestDto) {

        //how register api works ...?
        //it generate a OTP and send to make
        // verify it then it show activite --save as in active


        //else...by defualt create right now... active and save to db
        //check already present
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }


        //mapper use here
        User user = User.builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .enabled(true) //active
                .provider(Provider.LOCAL)
                .roles(userRequestDto.getRoles()) // default role can be set here, e.g.,
                .build();

        User savedUser = userRepository.save(user);


        return RegisterResponse.from(savedUser);
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequestDto, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );

        // 2. load user
        User user = userRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new InvalidResourceFoundException("Invalid email or password"));

        // 3. check enabled
        if (!user.isEnabled()) {
            throw new DisabledException("Account is deactivated, please contact admin");
        }

        // 4. persist refresh token
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTTL()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        // 5. generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        // 6. attach refresh token as HttpOnly cookie
        cookieService.attachRefreshTokenCookie(
                response,
                refreshToken,
                (int) jwtService.getRefreshTTL()
        );
        cookieService.addNoStoreHeaders(response);


        return new LoginResponse(accessToken, refreshToken, jwtService.getAccessTTL(), "Bearer");
        // 7. build response

    }
}
