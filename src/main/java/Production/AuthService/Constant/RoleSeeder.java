package Production.AuthService.Constant;

import Production.AuthService.entities.Role;
import Production.AuthService.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        for (String roleName : RoleConst.ALL_ROLES) {

            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        log.info("Creating role: {}", roleName);
                        return roleRepository.save(
                                Role.builder()
                                        .name(roleName)
                                        .build()
                        );
                    });
        }

        log.info("✅ Role seeding completed");
    }


}

