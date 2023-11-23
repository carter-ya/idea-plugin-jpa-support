package com.ifengxue.plugin.component;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.ifengxue.plugin.state.ModuleSettings;
import com.ifengxue.plugin.util.JavaLibraryUtils;
import com.ifengxue.plugin.util.StringHelper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.fields.ExpandableTextField;
import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AutoGeneratorSettings {

  private JPanel rootComponent;
  private ExpandableTextField textRemoveFieldPrefix;
  private ExpandableTextField textIfJavaKeywordAddSuffix;
  private ExpandableTextField textExtendBaseClass;
  private JCheckBox chkBoxUseLombok;
  private JCheckBox chkBoxGenerateRepository;
  private JCheckBox chkBoxSerializable;
  private JCheckBox chkBoxGenerateClassComment;
  private JCheckBox chkBoxGenerateFieldComment;
  private JCheckBox chkBoxGenerateMethodComment;
  private ExpandableTextField textExcludeFields;
  private JButton btnChooseSuperClass;
  private JCheckBox chkBoxUseJava8DateType;
  private ExpandableTextField textRemoveTablePrefix;
  private JLabel addTableNamePrefix;
  private ExpandableTextField textAddTableNamePrefix;
  private ExpandableTextField textAddTableNameSuffix;
  private MyPackageNameReferenceEditorCombo entityPackageReferenceEditorCombo;
  private MyPackageNameReferenceEditorCombo repositoryPackageReferenceEditorCombo;
  private JComboBox<String> cbxModule;
  private ExpandableTextField textEntityPackageParentPath;
  private ExpandableTextField textRepositoryPackageParentPath;
  private JCheckBox chkBoxGenerateDefaultValue;
  private JCheckBox chkBoxGenerateDatetimeDefaultValue;
  private JCheckBox chkBoxUseFluidProgrammingStyle;
  private ExpandableTextField textRepositorySuffix;
  private JCheckBox chkBoxGenerateSwaggerUIComment;
  private JCheckBox chkBoxTableNameAddSchemaName;
  private JCheckBox chkBoxGenerateJpaAnnotation;
  private JTabbedPane extensionPane;
  private JPanel controllerPane;
  private JPanel servicePane;
  private JPanel voPane;
  private ExpandableTextField textControllerPackageParentPath;
  private JCheckBox chkBoxGenerateController;
  private MyPackageNameReferenceEditorCombo controllerPackageReferenceEditorCombo;
  private JCheckBox chkBoxGenerateService;
  private ExpandableTextField textServicePackageParentPath;
  private JRadioButton radioJPA;
  private JRadioButton radioMybatisPlus;
  private JRadioButton radioTkMybatis;
  private MyPackageNameReferenceEditorCombo servicePackageReferenceEditorCombo;
  private JPanel dtoPane;
  private MyPackageNameReferenceEditorCombo voPackageReferenceEditorCombo;
  private ExpandableTextField textVOPackageParentPath;
  private JCheckBox chkBoxGenerateVO;
  private MyPackageNameReferenceEditorCombo dtoPackageReferenceEditorCombo;
  private ExpandableTextField textDTOPackageParentPath;
  private JCheckBox chkBoxGenerateDTO;
  private JCheckBox chkBoxGenerateEntity;
  private ExpandableTextField textVOSuffixName;
  private ExpandableTextField textDTOSuffixName;
  private MyPackageNameReferenceEditorCombo mapperXmlReferenceEditorCombo;
  private ExpandableTextField textMapperXmlParentPath;
  private JCheckBox chkBoxGenerateMapperXml;
  private ExpandableTextField textFileExtension;
  private JCheckBox chkBoxUseJakartaEE;
  private Color originalForegroundColor;

  private void createUIComponents() {
    entityPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("",
        Holder.getProject(), "", "Entity");
    repositoryPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("",
        Holder.getProject(), "",
        "Repository");
    controllerPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("",
        Holder.getProject(), "", "Controller");
    servicePackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("",
        Holder.getProject(), "", "Service");
    mapperXmlReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("", Holder.getProject(),
        "", "Mapper XML");
    voPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("", Holder.getProject(),
        "", "VO");
    dtoPackageReferenceEditorCombo = new MyPackageNameReferenceEditorCombo("", Holder.getProject(),
        "", "DTO");
  }

  public void setData(AutoGeneratorSettingsState data, @Nullable ModuleSettings moduleSettings) {
    if (originalForegroundColor == null) {
      originalForegroundColor = extensionPane.getForegroundAt(0);
    }

    textRemoveFieldPrefix.setText(data.getRemoveFieldPrefix());
    textIfJavaKeywordAddSuffix.setText(data.getIfJavaKeywordAddSuffix());
    chkBoxUseLombok.setSelected(data.isUseLombok());
    chkBoxSerializable.setSelected(data.isSerializable());
    chkBoxGenerateClassComment.setSelected(data.isGenerateClassComment());
    chkBoxGenerateFieldComment.setSelected(data.isGenerateFieldComment());
    chkBoxGenerateMethodComment.setSelected(data.isGenerateMethodComment());
    textExcludeFields.setText(String.join(",", data.getIgnoredFields()));
    textExtendBaseClass.setText(data.getInheritedParentClassName());
    chkBoxUseJava8DateType.setSelected(data.isUseJava8DateType());
    textRemoveTablePrefix.setText(data.getRemoveEntityPrefix());
    textAddTableNamePrefix.setText(data.getAddEntityPrefix());
    textAddTableNameSuffix.setText(data.getAddEntitySuffix());
    if (moduleSettings != null) {
      textFileExtension.setText(moduleSettings.getFileExtension());

      chkBoxGenerateEntity.setSelected(moduleSettings.isGenerateEntity());
      entityPackageReferenceEditorCombo.setText(moduleSettings.getEntityPackageName());
      textEntityPackageParentPath.setText(moduleSettings.getEntityParentDirectory());

      chkBoxGenerateRepository.setSelected(moduleSettings.isGenerateRepository());
      repositoryPackageReferenceEditorCombo.setText(moduleSettings.getRepositoryPackageName());
      textRepositoryPackageParentPath.setText(moduleSettings.getRepositoryParentDirectory());

      setForegroundColor(0, moduleSettings.isGenerateController());
      chkBoxGenerateController.setSelected(moduleSettings.isGenerateController());
      controllerPackageReferenceEditorCombo.setText(moduleSettings.getControllerPackageName());
      textControllerPackageParentPath.setText(moduleSettings.getControllerParentDirectory());

      setForegroundColor(1, moduleSettings.isGenerateService());
      chkBoxGenerateService.setSelected(moduleSettings.isGenerateService());
      servicePackageReferenceEditorCombo.setText(moduleSettings.getServicePackageName());
      textServicePackageParentPath.setText(moduleSettings.getServiceParentDirectory());
      radioJPA.setSelected(moduleSettings.isRepositoryTypeJPA());
      radioMybatisPlus.setSelected(moduleSettings.isRepositoryTypeMybatisPlus());
      radioTkMybatis.setSelected(moduleSettings.isRepositoryTypeTkMybatis());

      setForegroundColor(2, moduleSettings.isGenerateMapperXml());
      chkBoxGenerateMapperXml.setSelected(moduleSettings.isGenerateMapperXml());
      mapperXmlReferenceEditorCombo.setText(moduleSettings.getMapperXmlPackageName());
      textMapperXmlParentPath.setText(moduleSettings.getMapperXmlParentDirectory());

      setForegroundColor(3, moduleSettings.isGenerateVO());
      chkBoxGenerateVO.setSelected(moduleSettings.isGenerateVO());
      textVOSuffixName.setText(moduleSettings.getVoSuffixName());
      chkBoxGenerateVO.setText("Generate " + moduleSettings.getVoSuffixName());
      extensionPane.setTitleAt(3, textVOSuffixName.getText());
      voPackageReferenceEditorCombo.setText(moduleSettings.getVoPackageName());
      textVOPackageParentPath.setText(moduleSettings.getVoParentDirectory());

      setForegroundColor(4, moduleSettings.isGenerateDTO());
      chkBoxGenerateDTO.setSelected(moduleSettings.isGenerateDTO());
      textDTOSuffixName.setText(moduleSettings.getDtoSuffixName());
      chkBoxGenerateDTO.setText("Generate " + moduleSettings.getDtoSuffixName());
      extensionPane.setTitleAt(4, textDTOSuffixName.getText());
      dtoPackageReferenceEditorCombo.setText(moduleSettings.getDtoPackageName());
      textDTOPackageParentPath.setText(moduleSettings.getDtoParentDirectory());
    }
    chkBoxGenerateDefaultValue.setSelected(data.isGenerateDefaultValue());
    chkBoxGenerateDatetimeDefaultValue.setSelected(data.isGenerateDatetimeDefaultValue());
    chkBoxUseFluidProgrammingStyle.setSelected(data.isUseFluidProgrammingStyle());
    chkBoxGenerateSwaggerUIComment.setSelected(data.isGenerateSwaggerUIComment());
    chkBoxGenerateJpaAnnotation.setSelected(data.isGenerateJpaAnnotation());
    chkBoxTableNameAddSchemaName.setSelected(data.isAddSchemaNameToTableName());
    chkBoxUseJakartaEE.setSelected(data.isUseJakartaEE());
    if (!data.isUseJakartaEE()) {
      chkBoxUseJakartaEE.setSelected(JavaLibraryUtils.hasLibraryClass(Holder.getOrDefaultProject(),
          StringHelper.getJakartaEEClassNameOrNot(true, "Entity")));
    }
    textRepositorySuffix.setText(data.getRepositorySuffix());
  }

  public void setForegroundColor(int index, boolean isSelected) {
    extensionPane.setForegroundAt(index,
        isSelected ? JBColor.CYAN : originalForegroundColor);
  }

  public void getData(AutoGeneratorSettingsState data, ModuleSettings moduleData) {
    data.setRemoveFieldPrefix(textRemoveFieldPrefix.getText());
    data.setIfJavaKeywordAddSuffix(textIfJavaKeywordAddSuffix.getText());
    data.setUseLombok(chkBoxUseLombok.isSelected());
    data.setSerializable(chkBoxSerializable.isSelected());
    data.setGenerateClassComment(chkBoxGenerateClassComment.isSelected());
    data.setGenerateFieldComment(chkBoxGenerateFieldComment.isSelected());
    data.setGenerateMethodComment(chkBoxGenerateMethodComment.isSelected());
    data.setIgnoredFields(
        Arrays.stream(textExcludeFields.getText().split(",")).collect(Collectors.toSet()));
    data.setInheritedParentClassName(textExtendBaseClass.getText());
    data.setUseJava8DateType(chkBoxUseJava8DateType.isSelected());
    data.setRemoveEntityPrefix(textRemoveTablePrefix.getText());
    data.setAddEntityPrefix(textAddTableNamePrefix.getText());
    data.setAddEntitySuffix(textAddTableNameSuffix.getText());

    moduleData.setFileExtension(textFileExtension.getText());

    moduleData.setGenerateEntity(chkBoxGenerateEntity.isSelected());
    moduleData.setEntityPackageName(entityPackageReferenceEditorCombo.getText());
    moduleData.setRepositoryParentDirectory(textRepositoryPackageParentPath.getText());

    moduleData.setGenerateRepository(chkBoxGenerateRepository.isSelected());
    moduleData.setRepositoryPackageName(repositoryPackageReferenceEditorCombo.getText());
    moduleData.setEntityParentDirectory(textEntityPackageParentPath.getText());

    moduleData.setGenerateController(chkBoxGenerateController.isSelected());
    moduleData.setControllerPackageName(controllerPackageReferenceEditorCombo.getText());
    moduleData.setControllerParentDirectory(textControllerPackageParentPath.getText());

    moduleData.setGenerateService(chkBoxGenerateService.isSelected());
    moduleData.setServicePackageName(servicePackageReferenceEditorCombo.getText());
    moduleData.setServiceParentDirectory(textServicePackageParentPath.getText());
    moduleData.setRepositoryTypeJPA(radioJPA.isSelected());
    moduleData.setRepositoryTypeMybatisPlus(radioMybatisPlus.isSelected());
    moduleData.setRepositoryTypeTkMybatis(radioTkMybatis.isSelected());

    moduleData.setGenerateMapperXml(chkBoxGenerateMapperXml.isSelected());
    moduleData.setMapperXmlPackageName(mapperXmlReferenceEditorCombo.getText());
    moduleData.setMapperXmlParentDirectory(textMapperXmlParentPath.getText());

    moduleData.setGenerateVO(chkBoxGenerateVO.isSelected());
    moduleData.setVoSuffixName(textVOSuffixName.getText());
    moduleData.setVoPackageName(voPackageReferenceEditorCombo.getText());
    moduleData.setVoParentDirectory(textVOPackageParentPath.getText());

    moduleData.setGenerateDTO(chkBoxGenerateDTO.isSelected());
    moduleData.setDtoSuffixName(textDTOSuffixName.getText());
    moduleData.setDtoPackageName(dtoPackageReferenceEditorCombo.getText());
    moduleData.setDtoParentDirectory(textDTOPackageParentPath.getText());

    data.setModuleName(
        Optional.ofNullable(cbxModule.getSelectedItem()).map(Object::toString).orElse(""));
    data.setGenerateDefaultValue(chkBoxGenerateDefaultValue.isSelected());
    data.setGenerateDatetimeDefaultValue(chkBoxGenerateDatetimeDefaultValue.isSelected());
    data.setUseFluidProgrammingStyle(chkBoxUseFluidProgrammingStyle.isSelected());
    data.setGenerateSwaggerUIComment(chkBoxGenerateSwaggerUIComment.isSelected());
    data.setGenerateJpaAnnotation(chkBoxGenerateJpaAnnotation.isSelected());
    data.setAddSchemaNameToTableName(chkBoxTableNameAddSchemaName.isSelected());
    data.setUseJakartaEE(chkBoxUseJakartaEE.isSelected());
    data.setRepositorySuffix(textRepositorySuffix.getText());
  }
}
