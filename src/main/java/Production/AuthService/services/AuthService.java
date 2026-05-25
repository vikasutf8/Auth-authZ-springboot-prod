package Production.AuthService.services;

import Production.AuthService.dtos.Request.RegisterRequest;
import Production.AuthService.dtos.Response.RegisterResponse;

public interface AuthService {

    RegisterResponse registerUser(RegisterRequest userRequestDto);
}
