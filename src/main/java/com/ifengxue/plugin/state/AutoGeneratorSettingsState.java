package com.ifengxue.plugin.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Data
@State(name = "AutoGeneratorSettingsState", storages = {
    @Storage(value = StateConstants.PROJECT_STATE_NAME, roamingType = RoamingType.PER_OS),
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
   * 模块名称，模块配置
   */
  private Map<String, ModuleSettings> moduleNameToSettings;
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
  /**
   * 是否生成Swagger UI注释
   */
  private boolean generateSwaggerUIComment = false;

  @Nullable
  @Override
  public AutoGeneratorSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@Nonnull AutoGeneratorSettingsState state) {
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

  public ModuleSettings getModuleSettings() {
    if (StringUtils.isBlank(moduleName)) {
      throw new IllegalStateException("module name can't be empty");
    }
    return getModuleSettings(moduleName);
  }

  public ModuleSettings getModuleSettings(String moduleName) {
    if (moduleNameToSettings == null) {
      moduleNameToSettings = new HashMap<>();
    }
    return moduleNameToSettings.computeIfAbsent(moduleName, key -> new ModuleSettings());
  }
}
