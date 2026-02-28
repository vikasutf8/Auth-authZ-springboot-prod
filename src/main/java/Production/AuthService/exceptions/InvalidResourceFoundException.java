package Production.AuthService.exceptions;

public class InvalidResourceFoundException extends  RuntimeException{
    public InvalidResourceFoundException(String message) {
        super(message);
    }
}
