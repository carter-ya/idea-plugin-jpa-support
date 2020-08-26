package com.ifengxue.plugin.gui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableWidth {

    int UNSET_WIDTH = -1;

    int minWidth() default UNSET_WIDTH;

    int maxWidth() default UNSET_WIDTH;
}
