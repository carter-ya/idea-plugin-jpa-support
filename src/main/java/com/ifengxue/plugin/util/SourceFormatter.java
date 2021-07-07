package com.ifengxue.plugin.util;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import java.util.concurrent.atomic.AtomicInteger;

public enum SourceFormatter {
  ;
  private static final Logger log = Logger.getInstance(SourceFormatter.class);
  private static final AtomicInteger seq = new AtomicInteger();

  public static String formatJavaCode(String code) {
    try {
      Project project = Holder.getOrDefaultProject();
      PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
      PsiFile psiFile = psiFileFactory
          .createFileFromText(Constants.NAME + "_" + seq.getAndIncrement() + ".java", JavaFileType.INSTANCE, code);
      formatJavaCode(project, psiFile);
      return psiFile.getText();
    } catch (Exception e) {
      log.warn("Can't format java code", e);
      return code;
    }
  }

  public static void formatJavaCode(Project project, PsiFile psiFile) {
    JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
    try {
      javaCodeStyleManager.optimizeImports(psiFile);
      javaCodeStyleManager.shortenClassReferences(psiFile);
    } catch (Exception e) {
      log.error("optimize imports error", e);
    }
    try {
      CodeStyleManager.getInstance(project).reformat(psiFile);
    } catch (Exception e) {
      log.error("reformat source code error", e);
    }
  }
}
