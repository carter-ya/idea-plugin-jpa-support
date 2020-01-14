package fastjdbc.entity;

import fastjdbc.FastJdbcException;

public class Assert {

  public static EntityField assertNotNull(EntityField ef, String message) {
    if (ef != null) {
      return ef;
    }
    throw new FastJdbcException(message);
  }
}
