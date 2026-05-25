package Production.AuthService.services.implement;

import Production.AuthService.dtos.Request.RegisterRequest;
import Production.AuthService.dtos.Response.RegisterResponse;
import Production.AuthService.entities.User;
import Production.AuthService.entities.enums.Provider;
import Production.AuthService.repositories.UserRepository;
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
    private final UserRepository userRepository;
//    @Override
//    public Re registerUser(UserRequestDto userRequestDto) {
//
//        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
//        return userService.createUser(userRequestDto);
//    }

    @Override
    public RegisterResponse registerUser(RegisterRequest userRequestDto) {

        //how register api works ...?
        //it generate a OTP and send to make
        // verify it then it show activite --save as in active


        //else...by defualt create right now... active and save to db
        //check already present
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new RuntimeException("User with email " + userRequestDto.getEmail() + " already exists");
        }


        //mapper use here
        User user = User.builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .enabled(true) //active
                .provider(Provider.LOCAL)
                .roles(userRequestDto.getRoles()) // default role can be set here, e.g.,
                .build();

        User savedUser = userRepository.save(user);


        return RegisterResponse.from(savedUser);
    }
}
