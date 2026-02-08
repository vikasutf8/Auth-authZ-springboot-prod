package Production.AuthService.services.implement;

import Production.AuthService.dtos.RoleDto;
import Production.AuthService.dtos.UserRequestDto;
import Production.AuthService.dtos.UserResponseDto;
import Production.AuthService.entities.Role;
import Production.AuthService.entities.User;
import Production.AuthService.exceptions.ResourceAlreadyExistsException;
import Production.AuthService.exceptions.ResourceNotFoundException;
import Production.AuthService.repositories.RoleRepository;
import Production.AuthService.repositories.UserRepository;
import Production.AuthService.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Checking user is exist or not");
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + userRequestDto.getEmail());
        }
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
        log.info("User created successfully check to db");
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");

        List<User> users = userRepository.findAll();

        List<UserResponseDto> response = users.stream().map(user ->
                modelMapper.map(user, UserResponseDto.class)).toList();

        log.info("Total users fetched: {}", response.size());
        return response;
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        return modelMapper.map(user, UserResponseDto.class);
//        return  ResponseEntity.ok(userResponseDto);
    }

    @Override
    public String deleteUser(UUID userId) {
        if(!userRepository.existsById(userId)){
            throw new ResourceNotFoundException("User Not found"+ userId);
        }

        userRepository.deleteById(userId);
        return "User deleted Successfully";
    }

    @Override
    public UserResponseDto updateUser(UserRequestDto userRequestDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));
        user.setName(userRequestDto.getName());
        user.setImageUri(userRequestDto.getImageUri());
        user.setEnable(userRequestDto.isEnable());
//        user.setProvider(userRequestDto.getProvider());

        // roles – optional (only if provided)
//        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
//            user.setRoles(mapRoles(dto.getRoles()));
//        }

        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserResponseDto.class);


    }

    private Set<Role> mapRoles(Set<RoleDto> roleDtos) {
        if (roleDtos == null) return Set.of();

        return roleDtos.stream().map(dto -> Role.builder()// if exists
                .name(dto.getName())    // ROLE_USER, ROLE_ADMIN
                .build()).collect(Collectors.toSet());
    }
}


//  Risk in using Many to many
/*****
 * ModelMapper will:* create new Role entitiesw
 * ignore existing DB roles
 * try to insert them again
 * causes
 * constraint violation,duplicate roles or empty user_roles table
 *
 *
 *     //        User user = User.builder()
 * //                .email(userRequestDto.getEmail())
 * //                .name(userRequestDto.getName())
 * //                .password(userRequestDto.getPassword())
 * //                .imageUri(userRequestDto.getImageUri())
 * //                .enable(userRequestDto.isEnable())
 * //                .provider(userRequestDto.getProvider())
 * //                .roles(mapRoles(userRequestDto.getRoles()))
 * //                .build();
 */