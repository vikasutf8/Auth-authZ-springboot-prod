package Production.AuthService.services.implement;

import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImple implements AuthService {


    @Override
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        return null;
    }
}
