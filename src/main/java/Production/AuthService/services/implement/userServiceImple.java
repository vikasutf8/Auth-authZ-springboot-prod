package Production.AuthService.services.implement;

import Production.AuthService.dtos.RoleDto;
import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.entities.Role;
import Production.AuthService.entities.User;
import Production.AuthService.repositories.RoleRepository;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class userServiceImple implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    @Override
    @Transactional
    public ResponseEntity<UserResponseDto> createUser(UserRequestDto userRequestDto) {

        if(userRequestDto.getEmail() ==null || userRequestDto.getEmail().isBlank()){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
        // check already exist
        log.info("Checking user is exist or not");
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        // map basic fields\ log.info("Checking user is exist or not");
        log.info("user already not exist");
        User user = modelMapper.map(userRequestDto, User.class);

        // handle roles safely
//        Set<Role> roles = userRequestDto.getRoles().stream()
//                .map(roleDto ->
//                        roleRepository.findById(roleDto.getId())
//                                .orElseThrow(() ->
//                                        new RuntimeException("Role not found: " + roleDto.getId())
//                                )
//                )
//                .collect(Collectors.toSet());

        user.setRoles(null); //explicit handle

        User savedUser = userRepository.save(user);

        UserResponseDto responseDto = modelMapper.map(savedUser, UserResponseDto.class);

        log.info("User created successfully check to db");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @Override
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("Fetching all users");

        List<User> users = userRepository.findAll();

        List<UserResponseDto> response = users.stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();

        log.info("Total users fetched: {}", response.size());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponseDto> getUserByEmail(String email) {

        User user= userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("User not found"));

        UserResponseDto userResponseDto =modelMapper.map(user,UserResponseDto.class);
        return  ResponseEntity.ok(userResponseDto);
    }
    //        User user = User.builder()
//                .email(userRequestDto.getEmail())
//                .name(userRequestDto.getName())
//                .password(userRequestDto.getPassword())
//                .imageUri(userRequestDto.getImageUri())
//                .enable(userRequestDto.isEnable())
//                .provider(userRequestDto.getProvider())
//                .roles(mapRoles(userRequestDto.getRoles()))
//                .build();
    private Set<Role> mapRoles(Set<RoleDto> roleDtos) {
        if (roleDtos == null) return Set.of();

        return roleDtos.stream()
                .map(dto -> Role.builder()// if exists
                        .name(dto.getName())    // ROLE_USER, ROLE_ADMIN
                        .build()
                )
                .collect(Collectors.toSet());
    }
}
//  Risk in using Many to many
/*****
 * ModelMapper will:* create new Role entitiesw
 * ignore existing DB roles
 * try to insert them again
 * causes
 * constraint violation,duplicate roles or empty user_roles table
 */