package fastjdbc;

import java.io.Closeable;
import java.io.IOException;

public class IOUtil {

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }

  public static void close(Closeable... closeables) {
    for (Closeable closeable : closeables) {
      close(closeable);
    }
  }

  public static void close(AutoCloseable autoCloseable) {
    if (autoCloseable != null) {
      try {
        autoCloseable.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  public static void close(AutoCloseable... autoCloseables) {
    for (AutoCloseable autoCloseable : autoCloseables) {
      close(autoCloseable);
    }
  }
}
