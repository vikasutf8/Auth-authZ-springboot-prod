package Production.AuthService.entities;

import Production.AuthService.entities.enums.Provider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User {

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
}
