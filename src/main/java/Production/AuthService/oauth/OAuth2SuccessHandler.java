package Production.AuthService.oauth;

import Production.AuthService.SecurityUtils.CookieService;
import Production.AuthService.SecurityUtils.JwtService;
import Production.AuthService.entities.RefreshToken;
import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import Production.AuthService.repositories.RefreshTokenRepository;
import Production.AuthService.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {


    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontEndSuccessUrl;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        log.info("OAuth2 authentication success");

        OAuth2User oAuth2User =
                (OAuth2User) authentication.getPrincipal();

        String registrationId =
                ((OAuth2AuthenticationToken) authentication)
                        .getAuthorizedClientRegistrationId();

        log.info("Provider: {}{}", registrationId);

        // 🔹 Process user
        User user = processOAuthUser(registrationId, oAuth2User);
        log.info("{}user", user); //hole user i recived here ....from db
//     SKIP
    // 🔹 Revoke old refresh tokens (recommended) /// what we dont do
//        refreshTokenRepository
//                .revokeAllValidTokensByUser(user.getId());

        // 🔹 Create new refresh token entry (DB)
        String jti = UUID.randomUUID().toString();

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createAt(Instant.now())
                .expiresAt(
                        Instant.now()
                                .plusSeconds(jwtService.getRefreshTTL())
                )
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        // 🔹 Generate JWTs
        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user, jti);

        // 🔹 Attach refresh token in HttpOnly cookie
        cookieService.attachRefreshTokenCookie(
                response,
                refreshToken,
                (int) jwtService.getRefreshTTL()
        );

        // 🔹 Redirect frontend with access token
//        response.sendRedirect(
//                frontEndSuccessUrl + "?accessToken=" + accessToken
//        );
        response.getWriter().write("Login successful");
    }

    // =======================================================
    // 🔹 OAuth User Mapping
    // =======================================================

    private User processOAuthUser(String provider,
                                  OAuth2User oAuth2User) {

        return switch (provider) {

            case "google" -> handleGoogleUser(oAuth2User);

            case "github" -> handleGithubUser(oAuth2User);

            default -> throw new IllegalStateException(
                    "Unsupported provider: " + provider
            );
        };
    }

    private User handleGoogleUser(OAuth2User oAuth2User) {

        String providerId =
                String.valueOf(Objects.requireNonNull(oAuth2User.getAttribute("sub")));

        String email =
                oAuth2User.getAttribute("email");

        String name =
                oAuth2User.getAttribute("name");

        String picture =
                oAuth2User.getAttribute("picture");

        return userRepository
                .findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .imageUri(picture)
                                .enable(true)
                                .provider(Provider.GOOGLE)
                                .providerId(providerId)
                                .build()
                ));

        // have to defined roles also
    }

    private User handleGithubUser(OAuth2User oAuth2User) {

        String providerId =
                String.valueOf(Objects.requireNonNull(oAuth2User.getAttribute("id")));

        String name =
                oAuth2User.getAttribute("login");

        String image =
                oAuth2User.getAttribute("avatar_url");

        String email =
                oAuth2User.getAttribute("email"); // may be null

        return userRepository
                .findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .imageUri(image)
                                .enable(true)
                                .provider(Provider.GITHUB)
                                .providerId(providerId)
                                .build()
                ));
    }
}
