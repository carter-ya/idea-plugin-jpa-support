package fastjdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.Data;

@Data
public class Sql {

  private final String sql;
  private final List<Object> args;

  public void outputArgs(PreparedStatement preStat) throws SQLException {
    int index = 1;
    for (Object arg : args) {
      preStat.setObject(index++, arg);
    }
  }
}
