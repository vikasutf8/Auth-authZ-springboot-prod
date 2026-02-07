package Production.AuthService.services;

import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface UserService {

    ResponseEntity<UserResponseDto> createUser(UserRequestDto userRequestDto);
    ResponseEntity<List<UserResponseDto>> getAllUsers();
}
