package Production.AuthService.controllers;


import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v3/auth")
public class AuthController {

    private  final AuthService authService;

    @PostMapping
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.registerUser(userRequestDto));
    }
}
