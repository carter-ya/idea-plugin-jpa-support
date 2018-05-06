package com.ifengxue.plugin.component;

import com.intellij.openapi.project.Project;
import java.io.Serializable;
import java.util.Set;

public class AutoGeneratorConfig implements Serializable {

  private static final long serialVersionUID = -8978327106950673087L;
  private String removeTablePrefix;
  private String removeFieldPrefix;
  private String extendBaseClass;
  private String entityPackage;
  private String repositoryPackage;
  private Set<String> excludeFields;
  /**
   * equal to {@link Project#getBasePath()}
   */
  private String projectBasePath;
  private String entityDirectory;
  private String repositoryDirectory;
  private boolean useLombok;
  private boolean generateRepository;
  private boolean implementSerializable;
  private boolean generateClassComment;
  private boolean generateFieldComment;
  private boolean generateMethodComment;

  public String getRemoveTablePrefix() {
    return removeTablePrefix;
  }

  public void setRemoveTablePrefix(String removeTablePrefix) {
    this.removeTablePrefix = removeTablePrefix;
  }

  public String getRemoveFieldPrefix() {
    return removeFieldPrefix;
  }

  public void setRemoveFieldPrefix(String removeFieldPrefix) {
    this.removeFieldPrefix = removeFieldPrefix;
  }

  public String getExtendBaseClass() {
    return extendBaseClass;
  }

  public void setExtendBaseClass(String extendBaseClass) {
    this.extendBaseClass = extendBaseClass;
  }

  public String getEntityPackage() {
    return entityPackage;
  }

  public void setEntityPackage(String entityPackage) {
    this.entityPackage = entityPackage;
  }

  public String getRepositoryPackage() {
    return repositoryPackage;
  }

  public void setRepositoryPackage(String repositoryPackage) {
    this.repositoryPackage = repositoryPackage;
  }

  public boolean isUseLombok() {
    return useLombok;
  }

  public void setUseLombok(boolean useLombok) {
    this.useLombok = useLombok;
  }

  public boolean isGenerateRepository() {
    return generateRepository;
  }

  public void setGenerateRepository(boolean generateRepository) {
    this.generateRepository = generateRepository;
  }

  public boolean isImplementSerializable() {
    return implementSerializable;
  }

  public void setImplementSerializable(boolean implementSerializable) {
    this.implementSerializable = implementSerializable;
  }

  public boolean isGenerateClassComment() {
    return generateClassComment;
  }

  public void setGenerateClassComment(boolean generateClassComment) {
    this.generateClassComment = generateClassComment;
  }

  public boolean isGenerateFieldComment() {
    return generateFieldComment;
  }

  public void setGenerateFieldComment(boolean generateFieldComment) {
    this.generateFieldComment = generateFieldComment;
  }

  public boolean isGenerateMethodComment() {
    return generateMethodComment;
  }

  public void setGenerateMethodComment(boolean generateMethodComment) {
    this.generateMethodComment = generateMethodComment;
  }

  public Set<String> getExcludeFields() {
    return excludeFields;
  }

  public AutoGeneratorConfig setExcludeFields(Set<String> excludeFields) {
    this.excludeFields = excludeFields;
    return this;
  }

  public String getProjectBasePath() {
    return projectBasePath;
  }

  public AutoGeneratorConfig setProjectBasePath(String projectBasePath) {
    this.projectBasePath = projectBasePath;
    return this;
  }

  public String getEntityDirectory() {
    return entityDirectory;
  }

  public AutoGeneratorConfig setEntityDirectory(String entityDirectory) {
    this.entityDirectory = entityDirectory;
    return this;
  }

  public String getRepositoryDirectory() {
    return repositoryDirectory;
  }

  public AutoGeneratorConfig setRepositoryDirectory(String repositoryDirectory) {
    this.repositoryDirectory = repositoryDirectory;
    return this;
  }

  @Override
  public String toString() {
    return "AutoGeneratorConfig{" +
        "removeTablePrefix='" + removeTablePrefix + '\'' +
        ", removeFieldPrefix='" + removeFieldPrefix + '\'' +
        ", extendBaseClass='" + extendBaseClass + '\'' +
        ", entityPackage='" + entityPackage + '\'' +
        ", repositoryPackage='" + repositoryPackage + '\'' +
        ", useLombok=" + useLombok +
        ", generateRepository=" + generateRepository +
        ", implementSerializable=" + implementSerializable +
        ", generateClassComment=" + generateClassComment +
        ", generateFieldComment=" + generateFieldComment +
        ", generateMethodComment=" + generateMethodComment +
        '}';
  }
}
