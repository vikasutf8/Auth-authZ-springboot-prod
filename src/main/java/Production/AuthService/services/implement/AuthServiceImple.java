package Production.AuthService.services.implement;

import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.services.AuthService;
import Production.AuthService.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImple implements AuthService {

    private  final UserService userService;
    private  final PasswordEncoder passwordEncoder;
    @Override
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {

        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        return null;
    }
}
