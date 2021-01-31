package com.ifengxue.plugin.util;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import java.util.Objects;

public class Editors {

    public static Editor createSourceEditor(Project project, Language language, String content, boolean readOnly) {
        final EditorFactory factory = EditorFactory.getInstance();
        LanguageFileType fileType = language.getAssociatedFileType();
        final Editor editor = factory.createEditor(factory.createDocument(content), project,
            Objects.requireNonNull(fileType), readOnly);
        editor.getSettings().setRefrainFromScrolling(false);
        return editor;
    }

    public static void release(Editor editor) {
        EditorFactory.getInstance().releaseEditor(editor);
    }
}
