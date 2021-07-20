package exception;

public class DaoException extends RuntimeException {
    public DaoException(String errorString) {
        super(errorString);
    }
}
