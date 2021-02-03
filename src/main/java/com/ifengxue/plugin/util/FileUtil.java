package com.ifengxue.plugin.util;

import com.intellij.ide.util.DirectoryUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.file.PsiFileImplUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    public static PsiDirectory mkdirs(PsiManager manager, Path path) {
        VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(path.toFile());
        if (vFile != null && vFile.exists()) {
            return manager.findDirectory(vFile);
        }
        String directory = path.toAbsolutePath().toString().replace(File.separatorChar, '/');
        return DirectoryUtil.mkdirs(manager, directory);
    }
}
