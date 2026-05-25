package Production.AuthService.oAuth2;

import java.util.Map;

public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getProviderId();

    public abstract String getEmail();

    public abstract String getName();

    public abstract String getImageUri();
}
