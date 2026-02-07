package Production.AuthService.controllers;

import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v3/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<UserResponseDto> createUser(@Valid UserRequestDto userRequestDto){
        return userService.createUser(userRequestDto);
    }

    @GetMapping("all")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        return userService.getAllUsers();

        /*
        👉 REST API → Use List
👉 Batch / Streaming → Use Iterable
         */
    }
}
