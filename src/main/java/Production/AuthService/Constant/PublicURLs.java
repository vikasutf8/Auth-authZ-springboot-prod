package Production.AuthService.Constant;


public class PublicURLs {

    private PublicURLs() {
        // Prevent instantiation
    }

    // 🔓 Swagger & API Docs
    public static final String[] SWAGGER = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // 🔓 H2 Console (Dev Only)
    public static final String[] H2_CONSOLE = {
            "/h2-console/**"
    };

    // 🔓 Authentication Endpoints
    public static final String[] AUTH = {
            "/api/auth/login",
            "/api/auth/register"
    };

    // 🔓 OAuth2 Endpoints
    public static final String[] OAUTH = {
            "/oauth2/**",
            "/login/oauth2/**"
    };

    // 🔓 Actuator (Optional)
    public static final String[] ACTUATOR = {
            "/actuator/health"
    };

    public static final String[] ALL_PUBLIC_URL = {
            // Swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",

            // H2 Console (Dev)
            "/h2-console/**",

            // Auth APIs
            "/api/v3/auth/**",

            // OAuth2
            "/oauth2/**",
            "/login/oauth2/**",

            // Actuator (optional)
            "/actuator/health"
    };
}
