package Production.AuthService.Oidc;

import Production.AuthService.entities.Role;
import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import Production.AuthService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    // OidcUserService — Spring validates ID token, parses claims automatically
    // No manual /userinfo HTTP call needed unlike OAuth2

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);   // Spring validates ID token here

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // ID token claims directly — no extra HTTP call
        OidcUserInfo userInfo = oidcUser.getUserInfo();

        String email = oidcUser.getEmail();           // from ID token claim
        String name = oidcUser.getFullName();
        String imageUri = oidcUser.getPicture();
        String providerId = oidcUser.getSubject();         // "sub" claim — stable unique ID

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not present in ID token");
        }

        User user = userRepository.findByEmail(email)
                .map(existing -> updateExistingUser(existing, name, imageUri))
                .orElseGet(() -> createNewUser(email, name, imageUri, providerId, registrationId));

        // wrap back as OidcUser so Spring Security context stays intact
        return new DefaultOidcUser(
                user.getAuthorities(),
                oidcUser.getIdToken(),       // keep original ID token
                oidcUser.getUserInfo()
        );
    }

    private User createNewUser(
            String email,
            String name,
            String imageUri,
            String providerId,
            String registrationId
    ) {
        log.info("Creating new OIDC user: {}", email);

        return userRepository.save(
                User.builder()
                        .email(email)
                        .name(name)
                        .imageUri(imageUri)
                        .providerId(providerId)
                        .provider(Provider.valueOf(registrationId.toUpperCase()))
                        .roles(new HashSet<>(Set.of(Role.builder().build())))   // default role for OIDC users
                        .enabled(true)
                        .build()
        );
    }

    private User updateExistingUser(User existing, String name, String imageUri) {
        existing.setName(name);
        existing.setImageUri(imageUri);
        return userRepository.save(existing);
    }
}
