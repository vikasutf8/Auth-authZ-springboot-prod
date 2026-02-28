package Production.AuthService.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auth Service API",
                version = "v1",
                description = "JWT Authentication & Refresh Token API"
        )
)
public class OpenApiConfig {
}
