package Production.AuthService.dtos;

import Production.AuthService.entities.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    private String email;
    private String name;
    private String password;
    private String imageUri;

    private boolean enable =true;
    private Instant createdAt =Instant.now();
    private Instant updatedAt =Instant.now();

    private Provider provider =Provider.LOCAL;

    private Set<RoleDto> roles = new HashSet<>();
}
