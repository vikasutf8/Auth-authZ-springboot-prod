package Production.AuthService.controllers;

import Production.AuthService.config.SecurityConfig;
import Production.AuthService.SecurityUtils.JwtService;
import Production.AuthService.dtos.LoginRequestDto;
import Production.AuthService.dtos.TokenResponseDto;
import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.entities.User;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v3/auth")
public class AuthController {

    private  final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto){

        log.info(loginRequestDto.username(),loginRequestDto.password());
//authenticate
        Authentication authentication =authenticate(loginRequestDto);
        User user = userRepository.findByEmail(loginRequestDto.username()).orElseThrow(()-> new BadCredentialsException("Usernaem or Password are Invalid"));
        //check taht user enable --twice check
        if(!user.isEnable()){
            throw new DisabledException("DeActive User, Please connect admin");
        }

        //generate token
        String accessToken = jwtService.generateAccessToken(user);

        TokenResponseDto tokenResponseDto =TokenResponseDto.
                bearer(accessToken,"", jwtService.getAccessTTL(),"Bearer",modelMapper.map(user,UserResponseDto.class));

        return  ResponseEntity.ok(tokenResponseDto);
    }

    private Authentication authenticate(@Valid LoginRequestDto loginRequestDto) {
        try{
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.username(),loginRequestDto.password()));

        } catch (Exception e) {
            throw new RuntimeException("Usernaem or Password are Invalid");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.registerUser(userRequestDto));
//        return null;
    }
}
