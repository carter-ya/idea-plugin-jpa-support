package com.ifengxue.plugin.util;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.Holder;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
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

  public static void formatJavaCode(Project project, PsiFile psiFile) {
    JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
    try {
      javaCodeStyleManager.optimizeImports(psiFile);
      javaCodeStyleManager.shortenClassReferences(psiFile);
    } catch (Exception e) {
      log.error("optimize imports error", e);
    }
    formatByCodeStyle(project, psiFile);
  }

  public static String formatXml(String xml) {
    try {
      Project project = Holder.getOrDefaultProject();
      PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
      PsiFile psiFile = psiFileFactory
          .createFileFromText(Constants.NAME + "_" + seq.getAndIncrement() + ".xml",
              XmlFileType.INSTANCE, xml);
      formatByCodeStyle(project, psiFile);
      return psiFile.getText();
    } catch (Exception e) {
      log.warn("Can't format xml", e);
      return xml;
    }
  }

  public static void formatXml(Project project, PsiFile psiFile) {
    formatByCodeStyle(project, psiFile);
  }

  public static String format(String code, FileType fileType) {
    try {
      Project project = Holder.getOrDefaultProject();
      PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
      PsiFile psiFile = psiFileFactory.createFileFromText(
          Constants.NAME + "_" + seq.getAndIncrement() + "." + fileType.getDefaultExtension(),
          JavaFileType.INSTANCE, code);
      format(Holder.getOrDefaultProject(), psiFile, fileType);
      return psiFile.getText();
    } catch (Exception e) {
      log.warn("Can't format java code", e);
      return code;
    }
  }

  public static void format(Project project, PsiFile psiFile, FileType fileType) {
    if (fileType == XmlFileType.INSTANCE) {
      formatXml(project, psiFile);
    } else {
      formatJavaCode(project, psiFile);
    }
  }

  public static void formatByCodeStyle(Project project, PsiFile psiFile) {
    try {
      CodeStyleManager.getInstance(project).reformat(psiFile);
    } catch (Exception e) {
      log.error("reformat source code error", e);
    }
  }
}
