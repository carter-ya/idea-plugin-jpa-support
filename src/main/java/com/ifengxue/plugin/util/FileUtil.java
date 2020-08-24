package com.ifengxue.plugin.util;

import com.intellij.ide.util.DirectoryUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import java.io.File;
import java.nio.file.Path;

public enum FileUtil {
    ;

    public static PsiDirectory mkdirs(PsiManager manager, Path path) {
        String directory = path.toAbsolutePath().toString();
        if (File.separatorChar != '/') {
            if (directory.indexOf(File.separatorChar) != -1) {
                directory = directory.replace("\\", "/");
            }
        }
        return DirectoryUtil.mkdirs(manager, directory);
    }
}
