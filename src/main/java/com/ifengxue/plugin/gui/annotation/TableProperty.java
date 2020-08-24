package com.ifengxue.plugin.gui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * table property
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableProperty {

  int DEFAULT_INDEX = -1;

  String name() default "";

  String bundleName() default "";

  int index() default DEFAULT_INDEX;

  Class<?> columnClass() default NullClass.class;

  class NullClass {

  }
}
