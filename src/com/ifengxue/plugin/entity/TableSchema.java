package com.ifengxue.plugin.entity;

import java.io.Serializable;
import javax.persistence.Id;
import lombok.Data;

@Data
@javax.persistence.Table(name = "information_schema.TABLES")
public class TableSchema implements Serializable {

  private static final long serialVersionUID = 1853575310189734827L;
  @Id
  @javax.persistence.Column(name = "TABLE_NAME")
  private String tableName;

  @javax.persistence.Column(name = "TABLE_COMMENT")
  private String tableComment;

  @javax.persistence.Column(name = "TABLE_SCHEMA")
  private String tableSchema;
}
