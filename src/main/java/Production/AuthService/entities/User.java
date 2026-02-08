package Production.AuthService.entities;

import Production.AuthService.entities.enums.Provider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;
    @Column(name = "email",unique = true,nullable = false)
    private String email;
    private String name;
    private String password;
    private String imageUri;

    private boolean enable =true;
    private Instant createdAt =Instant.now();
    private Instant updatedAt =Instant.now();

    @Enumerated(EnumType.STRING)
    private Provider provider =Provider.LOCAL;

//    1 to n mapping --- Fk is stored here...owing side and reference at role call as invert side
//  n to m mapping -- also in some sencerios --join table

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    // explicitly changes value of createAt and updateAt at db time
    @PrePersist
    protected  void onCreated(){
        Instant now =Instant.now();
        if(createdAt ==null)createdAt=now;
        updatedAt=now;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roles
                .stream()
                .map(role ->
                        new SimpleGrantedAuthority(role.getName())).toList();
//        return List.of();
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
