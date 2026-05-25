package Production.AuthService.SecurityUtils;

import Production.AuthService.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*

Instead of using defualt UserDetailService and defuatl User -- we changes User Entity to imple UserDetails
and this custom class imple UserDetailsService


and bind that sense
userDetailSErvice imple UserDetails  --IN build
 */

@Service
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        /*
        this load user from db
         */
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
//        return userRepository.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("Invalid Password and Email"));
    }
}
