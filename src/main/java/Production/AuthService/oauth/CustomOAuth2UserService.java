package Production.AuthService.oauth;

import Production.AuthService.entities.Role;
import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import Production.AuthService.oAuth2.OAuth2UserInfo;
import Production.AuthService.oAuth2.OAuth2UserInfoFactory;
import Production.AuthService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);   // fetch from provider

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException("Email not returned by provider");
        }

        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existing -> updateExistingUser(existing, userInfo))   // returning user
                .orElseGet(() -> createNewUser(userInfo, registrationId)); // first time

        return new DefaultOAuth2User(
                user.getAuthorities(),
                oAuth2User.getAttributes(),
                "email"                        // name attribute key
        );
    }

    private User createNewUser(OAuth2UserInfo userInfo, String registrationId) {
        log.info("Creating new OAuth2 user: {}", userInfo.getEmail());

        User user = User.builder()
                .name(userInfo.getName())
                .email(userInfo.getEmail())
                .imageUri(userInfo.getImageUri())
                .provider(Provider.valueOf(registrationId.toUpperCase()))
                .providerId(userInfo.getProviderId())
                .roles(new HashSet<>(Set.of(Role.builder().build())))   // default role for OAuth users
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User existing, OAuth2UserInfo userInfo) {
        // only update name/image — never overwrite email or roles
        existing.setName(userInfo.getName());
        existing.setImageUri(userInfo.getImageUri());
        return userRepository.save(existing);
    }
}
