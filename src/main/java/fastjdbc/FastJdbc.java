package fastjdbc;

import fastjdbc.handler.ResultSetHandler;
import fastjdbc.handler.RowHandler;
import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;

public interface FastJdbc extends Closeable, AutoCloseable {
  // 增加方法

  /**
   * 保存实体
   *
   * @param entity 实体
   * @return 保存完成后的实体，该实体有可能会和参数实体不一致，因为主键是自增的
   */
  <T> T save(T entity) throws SQLException;

  /**
   * 批量保存实体
   *
   * @param entities 实体列表
   * @return 批量保存完成后的实体列表，实体列表可能会和参数实体不一致，因为主键是自增的
   */
  <T> List<T> save(List<T> entities) throws SQLException;

  // 删除方法

  /**
   * 删除单个实体
   *
   * @param entity 实体
   * @return 删除结果
   */
  <T> boolean delete(T entity) throws SQLException;

  /**
   * 删除多个实体
   *
   * @param entities 实体列表
   */
  <T> int delete(List<T> entities) throws SQLException;

  /**
   * 删除表中所有数据
   */
  void deleteAll(Class<?> entityClass) throws SQLException;

  /**
   * 删除单个实体
   *
   * @param id 主键
   * @param entityClass 实体类
   * @return 删除结果
   */
  <I> boolean deleteById(I id, Class<?> entityClass) throws SQLException;

  /**
   * 批量删除实体
   *
   * @param ids 主键列表
   * @param entityClass 实体类
   * @return 删除成功的数量
   */
  <I> int deleteByIds(List<I> ids, Class<?> entityClass) throws SQLException;

  /**
   * 根据属性删除实体
   *
   * @param fieldName 属性名
   * @param value 属性值
   * @param entityClass 实体类
   * @return 删除结果
   */
  boolean deleteByField(String fieldName, Object value, Class<?> entityClass) throws SQLException;

  /**
   * 根据属性值列表批量删除实体
   *
   * @param fieldName 属性名
   * @param values 属性值列表
   * @param entityClass 实体类
   */
  int deleteByField(String fieldName, List<Object> values, Class<?> entityClass) throws SQLException;

  /**
   * 根据sql批量删除实体
   *
   * @param sql sql
   * @param args 参数列表
   * @return 删除结果
   */
  int delete(String sql, Object... args) throws SQLException;

  // 修改方法

  /**
   * 更新实体所有字段
   *
   * @param entity 实体
   * @return 更新结果
   */
  <T> boolean update(T entity) throws SQLException;

  /**
   * 批量更新实体所有字段
   *
   * @param entities 实体列表
   * @return 更新结果
   */
  <T> int update(List<T> entities) throws SQLException;

  /**
   * 根据sql更新实体
   *
   * @param sql sql
   * @param args 参数列表
   */
  int update(String sql, Object... args) throws SQLException;

  // 查询方法

  /**
   * 根据主键查询实体
   *
   * @param primaryKey 主键
   * @param entityClass 实体类
   */
  <T, I> T findById(I primaryKey, Class<T> entityClass) throws SQLException;

  /**
   * 根据主键列表批量查询实体
   *
   * @param primaryKeys 主键列表
   * @param entityClass 实体类
   */
  <T, I> List<T> findByIds(List<I> primaryKeys, Class<T> entityClass) throws SQLException;

  /**
   * 根据属性名查询实体
   *
   * @param field 属性名
   * @param fieldValue 属性值
   * @param entityClass 实体类
   */
  <T> T findByField(String field, Object fieldValue, Class<T> entityClass) throws SQLException;

  /**
   * 根据属性名批量查询实体
   *
   * @param field 属性名
   * @param fieldValues 属性值列表
   * @param entityClass 实体类
   */
  <T> List<T> findByField(String field, List<Object> fieldValues, Class<T> entityClass) throws SQLException;

  /**
   * 根据sql批量查询sql
   *
   * @param sql sql
   * @param entityClass 实体类
   * @param args 参数列表
   */
  <T> List<T> find(String sql, Class<T> entityClass, Object... args) throws SQLException;

  /**
   * 根据sql批量查询sql
   *
   * @param sql sql
   * @param resultSetHandler result set handler
   * @param args 参数列表
   */
  <T> List<T> find(String sql, ResultSetHandler<T> resultSetHandler, Object... args) throws SQLException;

  /**
   * 根据sql批量查询sql
   *
   * @param sql sql
   * @param rowHandler row handler
   * @param args 参数列表
   */
  <T> List<T> find(String sql, RowHandler<T> rowHandler, Object... args) throws SQLException;

  /**
   * 根据sql查询一个实体
   *
   * @param sql sql
   * @param entityClass 实体类
   * @param args 参数列表
   */
  <T> T findOne(String sql, Class<T> entityClass, Object... args) throws SQLException;

  /**
   * 根据sql查询数量
   *
   * @param sql sql
   * @param entityClass 实体类
   * @param args 参数列表
   */
  <T> long count(String sql, Class<T> entityClass, Object... args) throws SQLException;
}
