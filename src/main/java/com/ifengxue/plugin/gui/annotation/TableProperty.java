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

  String name() default "";

  String bundleName() default "";

  int index() default -1;

  Class<?> columnClass() default NullClass.class;

  class NullClass {

  }
}
