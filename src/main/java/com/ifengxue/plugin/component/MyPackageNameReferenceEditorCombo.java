package com.ifengxue.plugin.component;

import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaCodeFragment.VisibilityChecker;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.ReferenceEditorComboWithBrowseButton;
import org.jetbrains.annotations.NotNull;

public class MyPackageNameReferenceEditorCombo extends ReferenceEditorComboWithBrowseButton {

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
        }
      }
    });
  }
}
