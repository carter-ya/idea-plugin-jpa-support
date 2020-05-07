package fastjdbc;

public class FastJdbcException extends RuntimeException {

  public FastJdbcException() {
  }

  public FastJdbcException(String message) {
    super(message);
  }

  public FastJdbcException(String message, Throwable cause) {
    super(message, cause);
  }

  public FastJdbcException(Throwable cause) {
    super(cause);
  }

  public FastJdbcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
