package Production.AuthService.services;

import Production.AuthService.dtos.LoginRequestDto;
import Production.AuthService.dtos.Request.RegisterRequest;
import Production.AuthService.dtos.Response.LoginResponse;
import Production.AuthService.dtos.Response.RegisterResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    RegisterResponse registerUser(RegisterRequest userRequestDto);
//    LoginResponse loginUser(LoginRequest loginRequestDto);

    LoginResponse loginUser(LoginRequestDto loginRequestDto, HttpServletResponse response);
}
