package Production.AuthService.services;

import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;


public interface UserService {

    UserResponseDto createUser(UserRequestDto userRequestDto);
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserByEmail(String email);
    String deleteUser(UUID userId);

    UserResponseDto updateUser(UserRequestDto userRequestDto, String email);
}
