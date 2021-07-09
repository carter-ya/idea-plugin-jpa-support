package com.ifengxue.plugin.generator.merge;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import javax.annotation.Nullable;

public class SourceFileMergerFactory {

    @Nullable
    public static SourceFileMerger createMerger(FileType fileType) {
        if (fileType == JavaFileType.INSTANCE) {
            return new JavaSourceFileMerger();
        }
        return null;
    }
}
