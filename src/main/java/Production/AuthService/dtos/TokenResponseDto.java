package Production.AuthService.dtos;

public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserResponseDto user
) {
    public static TokenResponseDto bearer(
            String accessToken,
            String refreshToken,

            long expiresIn,
            String tokenType,

            UserResponseDto user
    ) {
        return new TokenResponseDto(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}
