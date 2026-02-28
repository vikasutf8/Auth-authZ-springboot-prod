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
import java.util.UUID;

@RestController
@RequestMapping("api/v3/user")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto){
        UserResponseDto response = userService.createUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getAllUsers());
    }
        /*
        👉 REST API → Use List
👉 Batch / Streaming → Use Iterable
         */

        @GetMapping("/email/{email}")
        @ResponseStatus(HttpStatus.OK)
        @ResponseBody
        public ResponseEntity<UserResponseDto> getUser(@PathVariable String email){
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(email));
        }

        @DeleteMapping("/{userId}")
        @ResponseStatus(HttpStatus.OK)
        @ResponseBody
        public ResponseEntity<String> getUser(@PathVariable UUID userId){

            return ResponseEntity.status(HttpStatus.OK).body(userService.deleteUser(userId));
        }

        @PatchMapping("/email/{email}")
        @ResponseStatus(HttpStatus.CREATED)
        @ResponseBody
        public ResponseEntity<UserResponseDto> updateUser(@Valid @RequestBody UserRequestDto userRequestDto ,@PathVariable String email){
            return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(userRequestDto,email));
        }


}
