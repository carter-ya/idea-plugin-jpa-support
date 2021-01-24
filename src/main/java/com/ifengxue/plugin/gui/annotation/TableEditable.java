package com.ifengxue.plugin.gui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableEditable {

  /**
   * 是否可以编辑
   */
  boolean editable() default true;

  Class<? extends EditorProvider> editorProvider() default NullEditorProvider.class;

  Class<? extends RendererProvider> rendererProvider() default NullRendererProvider.class;

  Class<? extends PropertyEditorProvider> propertyEditorProvider() default NullPropertyEditorProvider.class;
}
