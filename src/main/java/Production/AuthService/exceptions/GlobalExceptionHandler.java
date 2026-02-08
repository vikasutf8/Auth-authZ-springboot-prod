package Production.AuthService.exceptions;


import Production.AuthService.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
            ErrorResponse internalServerError =new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);

       return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(internalServerError);
    }
}
