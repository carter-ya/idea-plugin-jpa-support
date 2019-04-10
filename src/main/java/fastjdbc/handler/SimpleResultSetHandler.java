package fastjdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SimpleResultSetHandler<T> implements ResultSetHandler {

  private final RowHandler<T> rowHandler;

  public SimpleResultSetHandler(RowHandler<T> rowHandler) {
    this.rowHandler = rowHandler;
  }

  @Override
  public List<T> handle(ResultSet rs) throws SQLException {
    int row = 0;
    List<T> results = new LinkedList<>();
    while (rs.next()) {
      results.add(rowHandler.handle(rs, row++));
    }
    return results;
  }
}
