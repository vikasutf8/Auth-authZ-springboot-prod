package Production.AuthService.dtos;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String message,
        HttpStatus status
//        int statusCode
        ) {

}
