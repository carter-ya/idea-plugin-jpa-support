package fastjdbc;

import static java.util.stream.Collectors.toList;

import fastjdbc.entity.Assert;
import fastjdbc.entity.Entity;
import fastjdbc.entity.EntityCacheManager;
import fastjdbc.entity.EntityUtil;
import fastjdbc.entity.ResultSetExtractor;
import fastjdbc.handler.ResultSetHandler;
import fastjdbc.handler.RowHandler;
import fastjdbc.handler.SimpleResultSetHandler;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;

public class SimpleFastJdbc implements FastJdbc {

  private final DataSource datasource;

  public SimpleFastJdbc(DataSource dataSource) {
    this.datasource = dataSource;
  }

  @Override
  public <T> T save(T entity) throws SQLException {
    Sql sql = SqlBuilder.newInsertBuilder(entity).insert(entity).build();
    Connection connection = null;
    PreparedStatement preStat = null;
    ResultSet rs = null;
    try {
      connection = datasource.getConnection();
      Entity cacheEntity = EntityCacheManager.getEntity(entity);
      if (cacheEntity.getIdField() == null) {
        preStat = connection.prepareStatement(sql.getSql());
      } else {
        preStat = connection.prepareStatement(sql.getSql(), Statement.RETURN_GENERATED_KEYS);
      }
      sql.outputArgs(preStat);
      preStat.executeUpdate();
      if (cacheEntity.getIdField() != null) {
        rs = preStat.getGeneratedKeys();
        rs.next();
        ResultSetExtractor.extract(1, rs, cacheEntity.getIdField(), entity);
      }
    } finally {
      IOUtil.close(rs, preStat, connection);
    }
    return entity;
  }

  @Override
  public <T> List<T> save(List<T> entities) throws SQLException {
    if (entities.isEmpty()) {
      return Collections.emptyList();
    }
    Connection connection = null;
    PreparedStatement preStat = null;
    ResultSet rs = null;
    try {
      connection = datasource.getConnection();
      Entity cacheEntity = null;
      for (T object : entities) {
        if (cacheEntity == null) {
          cacheEntity = EntityCacheManager.getEntity(object);
        }
        Sql sql = SqlBuilder.newInsertBuilder(object).insert(object).build();
        if (preStat == null) {
          if (cacheEntity.getIdField() != null) {
            preStat = connection.prepareStatement(sql.getSql(), Statement.RETURN_GENERATED_KEYS);
          } else {
            preStat = connection.prepareStatement(sql.getSql());
          }
        }
        sql.outputArgs(preStat);
        preStat.addBatch();
      }
      assert preStat != null;
      preStat.executeBatch();
      if (cacheEntity.getIdField() != null) {
        rs = preStat.getGeneratedKeys();
        for (T entity : entities) {
          if (rs.next()) {
            ResultSetExtractor.extract(1, rs, cacheEntity.getIdField(), entity);
          }
        }
      }
    } finally {
      IOUtil.close(rs, preStat, connection);
    }
    return entities;
  }

  @Override
  public <T> boolean delete(T entity) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entity);
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + entity.getClass() + "没有主键");
    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      connection = datasource.getConnection();
      Sql sql = SqlBuilder.newDeleteBuilder(entity).where().equal(cacheEntity.getIdField().getFieldName(),
          EntityUtil.readValue(entity, cacheEntity.getIdField())).build();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return 1 == preStat.executeUpdate();
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public <T> int delete(List<T> entities) throws SQLException {
    if (entities.isEmpty()) {
      return 0;
    }
    T first = entities.get(0);
    Entity cacheEntity = EntityCacheManager.getEntity(first);
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + first.getClass() + "没有主键");

    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      List<Object> primaryKeys = entities.stream()
          .map(entity -> EntityUtil.readValue(entity, cacheEntity.getIdField())).collect(toList());
      Sql sql = SqlBuilder.newDeleteBuilder(first).where()
          .in(cacheEntity.getIdField().getFieldName(), primaryKeys).build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return preStat.executeUpdate();
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public void deleteAll(Class<?> entityClass) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      Sql sql = SqlBuilder.newDeleteBuilder(entityClass).build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      preStat.executeUpdate();
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public <I> boolean deleteById(I id, Class<?> entityClass) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entityClass);
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + entityClass + "没有主键");

    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      Sql sql = SqlBuilder.newDeleteBuilder(entityClass).where()
          .equal(cacheEntity.getIdField().getFieldName(), id).build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return preStat.executeUpdate() == 1;
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public <I> int deleteByIds(List<I> ids, Class<?> entityClass) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entityClass);

    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      Sql sql = SqlBuilder.newDeleteBuilder(entityClass).where()
          .in(cacheEntity.getIdField().getFieldName(), ids)
          .build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return preStat.executeUpdate();
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public boolean deleteByField(String fieldName, Object value, Class<?> entityClass) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      Sql sql = SqlBuilder.newDeleteBuilder(entityClass).where().equal(fieldName, value).build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return preStat.executeUpdate() == 1;
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public int deleteByField(String fieldName, List<Object> values, Class<?> entityClass) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entityClass);
    // 检查字段是否存在
    EntityUtil.findColumnName(fieldName, cacheEntity);

    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      Sql sql = SqlBuilder.newDeleteBuilder(entityClass).where().in(fieldName, values).build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return preStat.executeUpdate();
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public int delete(String sql, Object... args) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql);
      int index = 1;
      for (Object arg : args) {
        preStat.setObject(index++, arg);
      }
      return preStat.executeUpdate();
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public <T> boolean update(T entity) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entity);
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + entity.getClass() + "没有主键");

    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      Sql sql = SqlBuilder.newUpdateBuilder(entity).update(entity).build();
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql.getSql());
      sql.outputArgs(preStat);
      return preStat.executeUpdate() == 1;
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public <T> int update(List<T> entities) throws SQLException {
    if (entities.isEmpty()) {
      return 0;
    }
    Entity cacheEntity = EntityCacheManager.getEntity(entities.get(0));
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + cacheEntity.getBeanClass() + "没有主键");

    Connection connection = null;
    PreparedStatement preStat = null;
    try {
      connection = datasource.getConnection();
      for (T entity : entities) {
        Sql sql = SqlBuilder.newUpdateBuilder(cacheEntity.getBeanClass()).update(entity).build();
        if (preStat == null) {
          preStat = connection.prepareStatement(sql.getSql());
        }
        sql.outputArgs(preStat);
        preStat.addBatch();
      }
      assert preStat != null;
      int sum = 0;
      for (int count : preStat.executeBatch()) {
        sum += count;
      }
      return sum;
    } finally {
      IOUtil.close(preStat, connection);
    }
  }

  @Override
  public int update(String sql, Object... args) throws SQLException {
    return delete(sql, args);
  }

  @Override
  public <T, I> T findById(I primaryKey, Class<T> entityClass) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entityClass);
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + entityClass + "没有主键");

    return findByField(cacheEntity.getIdField().getFieldName(), primaryKey, entityClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T, I> List<T> findByIds(List<I> primaryKeys, Class<T> entityClass) throws SQLException {
    Entity cacheEntity = EntityCacheManager.getEntity(entityClass);
    Assert.assertNotNull(cacheEntity.getIdField(), "实体" + entityClass + "没有主键");

    return findByField(cacheEntity.getIdField().getFieldName(), (List<Object>) primaryKeys, entityClass);
  }

  @Override
  public <T> T findByField(String field, Object fieldValue, Class<T> entityClass) throws SQLException {
    Sql sql = SqlBuilder.newSelectBuilder(entityClass).select().from().where().equal(field, fieldValue).build();

    List<T> entities = find(sql.getSql(), entityClass, sql.getArgs().toArray());

    if (entities.isEmpty()) {
      return null;
    }
    T firstEntity = null;
    for (T entity : entities) {
      if (firstEntity == null) {
        firstEntity = entity;
      } else {
        throw new FastJdbcException("条件" + field + "=" + fieldValue + "有超过一个查询结果，如果只需要一个结果，可以考虑使用findOne");
      }
    }
    return firstEntity;
  }

  @Override
  public <T> List<T> findByField(String field, List<Object> fieldValues, Class<T> entityClass)
      throws SQLException {
    Sql sql = SqlBuilder.newSelectBuilder(entityClass).select().from().where().in(field, fieldValues).build();
    return find(sql.getSql(), entityClass, sql.getArgs().toArray());
  }

  @Override
  public <T> List<T> find(String sql, Class<T> entityClass, Object... args) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    ResultSet rs = null;
    ResultSetMetaData rsMetaData = null;

    List<T> entities;
    try {
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql);
      rs = fillArgsAndQuery(preStat, args);

      Map<String, Integer> columnNameMap = null;
      Entity cacheEntity = EntityCacheManager.getEntity(entityClass);
      entities = new LinkedList<>();
      while (rs.next()) {
        // 解析查询列名
        if (rsMetaData == null) {
          rsMetaData = rs.getMetaData();
          columnNameMap = new HashMap<>(rsMetaData.getColumnCount());
          for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
            columnNameMap.put(rsMetaData.getColumnName(i), i);
          }
        }
        T entity = BeanUtil.instantiate(entityClass);
        for (Entry<String, Integer> entry : columnNameMap.entrySet()) {
          ResultSetExtractor
              .extract(entry.getValue(), rs, EntityUtil.findByColumnName(entry.getKey(), cacheEntity), entity);
        }
        entities.add(entity);
      }
    } finally {
      IOUtil.close(rs, preStat, connection);
    }
    return entities;
  }

  @Override
  public <T> List<T> find(String sql, ResultSetHandler<T> resultSetHandler, Object... args) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    ResultSet rs = null;

    try {
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql);
      rs = fillArgsAndQuery(preStat, args);

      return resultSetHandler.handle(rs);
    } finally {
      IOUtil.close(rs, preStat, connection);
    }
  }

  @Override
  public <T> List<T> find(String sql, RowHandler<T> rowHandler, Object... args) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    ResultSet rs = null;

    try {
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql);
      rs = fillArgsAndQuery(preStat, args);

      return new SimpleResultSetHandler<>(rowHandler).handle(rs);
    } finally {
      IOUtil.close(rs, preStat, connection);
    }
  }

  @Override
  public <T> T findOne(String sql, Class<T> entityClass, Object... args) throws SQLException {
    List<T> entities = find(sql, entityClass, args);
    if (entities.isEmpty()) {
      return null;
    }
    return entities.get(0);
  }

  @Override
  public <T> long count(String sql, Class<T> entityClass, Object... args) throws SQLException {
    Connection connection = null;
    PreparedStatement preStat = null;
    ResultSet rs = null;
    try {
      connection = datasource.getConnection();
      preStat = connection.prepareStatement(sql);

      rs = fillArgsAndQuery(preStat, args);
      rs.next();
      return rs.getLong(1);
    } finally {
      IOUtil.close(rs, preStat, connection);
    }
  }

  private ResultSet fillArgsAndQuery(PreparedStatement preStat, Object... args) throws SQLException {
    int index = 1;
    for (Object arg : args) {
      preStat.setObject(index++, arg);
    }
    return preStat.executeQuery();
  }

  @Override
  public void close() {
    if (datasource == null) {
      return;
    }
    if (datasource instanceof Closeable) {
      IOUtil.close((Closeable) datasource);
    } else if (datasource instanceof AutoCloseable) {
      IOUtil.close((AutoCloseable) datasource);
    }
  }
}
