package Production.AuthService.dtos.Response;

import Production.AuthService.entities.Role;
import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private UUID id;
    private String name;
    private String email;
    private String imageUri;
    private boolean enabled;
    private Provider provider;
    private Set<Role> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public static RegisterResponse from(User user) {
        return RegisterResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .imageUri(user.getImageUri())
                .enabled(user.isEnabled())
                .provider(user.getProvider())
                .roles(Collections.unmodifiableSet(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
