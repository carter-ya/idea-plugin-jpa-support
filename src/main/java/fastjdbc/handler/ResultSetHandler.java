package fastjdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetHandler<T> {

  List<T> handle(ResultSet rs) throws SQLException;
}
