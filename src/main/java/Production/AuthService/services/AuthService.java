package Production.AuthService.services;

import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;

public interface AuthService {

    public UserResponseDto registerUser(UserRequestDto userRequestDto);
}
