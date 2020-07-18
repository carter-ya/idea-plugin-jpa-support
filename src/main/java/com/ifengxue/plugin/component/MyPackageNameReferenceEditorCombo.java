package com.ifengxue.plugin.component;

import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragment.VisibilityChecker;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.ReferenceEditorComboWithBrowseButton;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class MyPackageNameReferenceEditorCombo extends ReferenceEditorComboWithBrowseButton {

  /**
   * 被选择的包的路径
   */
  @Setter
  @Getter
  private String selectedPackagePath;

  public MyPackageNameReferenceEditorCombo(String text, @NotNull Project project, String recentsKey,
      String chooserTitle) {
    super(null, text, project, false, VisibilityChecker.PROJECT_SCOPE_VISIBLE, recentsKey);
    getButton().setText(LocaleContextHolder.format("button_browse"));
    addActionListener(event -> {
      final PackageChooserDialog chooser = new PackageChooserDialog(chooserTitle, project);
      chooser.selectPackage(getText());
      if (chooser.showAndGet()) {
        final PsiPackage aPackage = chooser.getSelectedPackage();
        if (aPackage != null) {
          setText(aPackage.getQualifiedName());
          setSelectedPackagePath(
              aPackage.getDirectories(GlobalSearchScope.projectScope(project))[0].getVirtualFile().getPath());
        } else {
          setSelectedPackagePath(null);
        }
      }
    });
  }
}
