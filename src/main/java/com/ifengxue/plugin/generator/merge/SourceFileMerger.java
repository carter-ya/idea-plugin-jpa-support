package com.ifengxue.plugin.generator.merge;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.intellij.psi.PsiFile;

public interface SourceFileMerger {

    boolean tryMerge(GeneratorConfig generatorConfig, Table table, PsiFile originalFile,
        PsiFile newFile);

    boolean isMergeSupported();
}
