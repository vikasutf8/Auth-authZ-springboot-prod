package Production.AuthService.SecurityUtils;

import Production.AuthService.entities.User;
import Production.AuthService.exceptions.ResourceNotFoundException;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*
        this load user from db
         */
        User user =userRepository.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("Invalid Password and Email"));
        return  user;
    }
}
