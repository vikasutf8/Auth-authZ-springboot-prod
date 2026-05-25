package Production.AuthService.dtos.Response;


public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
}