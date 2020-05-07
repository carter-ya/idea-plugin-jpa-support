package fastjdbc.entity;

import fastjdbc.FastJdbcException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ResultSetExtractor {

  public static void extract(int columnIndex, ResultSet rs, EntityField ef, Object entity) throws SQLException {
    Class<?> javaType = ef.getJavaType();
    try {
      if (javaType == byte.class || javaType == Byte.class) {
        ef.getWriteMethod().invoke(entity, rs.getByte(columnIndex));
      } else if (javaType == short.class || javaType == Short.class) {
        ef.getWriteMethod().invoke(entity, rs.getShort(columnIndex));
      } else if (javaType == int.class || javaType == Integer.class) {
        ef.getWriteMethod().invoke(entity, rs.getInt(columnIndex));
      } else if (javaType == long.class || javaType == Long.class) {
        ef.getWriteMethod().invoke(entity, rs.getLong(columnIndex));
      } else if (javaType == float.class || javaType == Float.class) {
        ef.getWriteMethod().invoke(entity, rs.getFloat(columnIndex));
      } else if (javaType == double.class || javaType == Double.class) {
        ef.getWriteMethod().invoke(entity, rs.getDouble(columnIndex));
      } else if (javaType == boolean.class || javaType == Boolean.class) {
        ef.getWriteMethod().invoke(entity, rs.getBoolean(columnIndex));
      } else if (javaType == String.class) {
        ef.getWriteMethod().invoke(entity, rs.getString(columnIndex));
      } else if (javaType == byte[].class) {
        ef.getWriteMethod().invoke(entity, (Object) rs.getBytes(columnIndex));
      } else if (javaType == InputStream.class) {
        ef.getWriteMethod().invoke(entity, rs.getBinaryStream(columnIndex));
      } else if (javaType == Reader.class) {
        ef.getWriteMethod().invoke(entity, rs.getCharacterStream(columnIndex));
      } else if (javaType == Date.class) {
        ef.getWriteMethod().invoke(entity, rs.getDate(columnIndex));
      } else if (javaType == Timestamp.class) {
        ef.getWriteMethod().invoke(entity, rs.getTimestamp(columnIndex));
      } else if (javaType == java.util.Date.class) {
        ef.getWriteMethod().invoke(entity, rs.getTimestamp(columnIndex));
      } else {
        ef.getWriteMethod().invoke(entity, rs.getObject(columnIndex));
      }
    } catch (ReflectiveOperationException e) {
      throw new FastJdbcException(e);
    }
  }

}
