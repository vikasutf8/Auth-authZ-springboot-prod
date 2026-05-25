package Production.AuthService.entities;

import Production.AuthService.entities.enums.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")                          // JPQL name: SELECT u FROM User u
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),   // auth lookup
                @Index(name = "idx_users_provider_id", columnList = "provider_id") // OAuth lookup
        }
)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "password")                  // nullable: OAuth users have no password
    private String password;

    @Column(name = "image_uri", length = 512)
    private String imageUri;

    @Builder.Default                             // fix: Builder respects this default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @CreationTimestamp                           // Hibernate-managed; no manual @PrePersist needed
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp                             // Hibernate-managed
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    // --- Roles ---

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)  // roles needed on every auth check → EAGER ok
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            indexes = @Index(name = "idx_user_roles_user_id", columnList = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Set<Role> roles = new HashSet<>();

    // --- UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;                            // fix: was returning ""
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired
    // default to true via UserDetails interface — override only if you need that logic
}