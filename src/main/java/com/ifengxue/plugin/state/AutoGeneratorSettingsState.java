package com.ifengxue.plugin.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Data
@State(name = "AutoGeneratorSettingsState", storages = {
    @Storage(value = StateConstants.APPLICATION_STATE_NAME, roamingType = RoamingType.PER_OS),
})
public class AutoGeneratorSettingsState implements PersistentStateComponent<AutoGeneratorSettingsState> {

  /**
   * 被移除的实体前缀
   */
  private String removeEntityPrefix = "t_";
  /**
   * 被添加的实体前缀
   */
  private String addEntityPrefix = "";
  /**
   * 被添加的实体后缀
   */
  private String addEntitySuffix = "";
  /**
   * Repository后缀
   */
  private String repositorySuffix = "Repository";
  /**
   * 模块名称
   */
  private String moduleName = "";
  /**
   * 被移除的字段前缀
   */
  private String removeFieldPrefix = "f_";
  /**
   * 继承的父类名称
   */
  private String inheritedParentClassName = "";
  /**
   * 忽略的字段列表
   */
  private Set<String> ignoredFields = new HashSet<>();
  /**
   * 实体包名
   */
  private String entityPackageName = "";
  /**
   * 实体包parent路径<br>
   * 如: /path/to/maven/module/src/main/java
   */
  private String entityParentDirectory = "";
  /**
   * repository包名
   */
  private String repositoryPackageName = "";
  /**
   * repository包名<br>
   * 如:/path/to/maven/module/src/main/java
   */
  private String repositoryParentDirectory = "";
  /**
   * 是否使用Lombok<a href="https://projectlombok.org/">Lombok</a>
   */
  private boolean useLombok = true;
  /**
   * 是否生成Repository
   */
  private boolean generateRepository = true;
  /**
   * 实体是否实现{@link java.io.Serializable}
   */
  private boolean serializable = true;
  /**
   * 是否生成类注释
   */
  private boolean generateClassComment = true;
  /**
   * 是否生成字段注释
   */
  private boolean generateFieldComment = true;
  /**
   * 是否生成方法注释
   */
  private boolean generateMethodComment = true;
  /**
   * 是否使用Java 8日期时间类型
   *
   * @see java.time.LocalDateTime
   */
  private boolean useJava8DateType;
  /**
   * 生成字段默认值（仅在列声明了默认值时有效）
   */
  private boolean generateDefaultValue = false;
  /**
   * 生成日期时间字段默认值（仅在列声明了默认值时有效）
   */
  private boolean generateDatetimeDefaultValue = false;
  /**
   * 是否使用流式编程风格
   */
  private boolean useFluidProgrammingStyle = false;

  @Nullable
  @Override
  public AutoGeneratorSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(AutoGeneratorSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String removeTablePrefix(String tableName) {
    if (!removeEntityPrefix.isEmpty() && tableName.startsWith(removeEntityPrefix)) {
      return tableName.substring(removeEntityPrefix.length());
    }
    return tableName;
  }

  public String concatPrefixAndSuffix(String entityName) {
    return addEntityPrefix + entityName + addEntitySuffix;
  }
}
