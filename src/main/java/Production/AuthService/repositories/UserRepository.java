package Production.AuthService.repositories;

import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByProviderAndProviderId(
            Provider provider,
            String providerId
    );
}
