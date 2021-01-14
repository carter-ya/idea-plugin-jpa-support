package com.ifengxue.plugin.state.converter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.Converter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassConverter extends Converter<Class<?>> {

    private static final Logger log = Logger.getInstance(ClassConverter.class);

    @Override
    public @Nullable Class<?> fromString(@NotNull String value) {
        try {
            return StringUtils.isBlank(value) ? null : Class.forName(value);
        } catch (ClassNotFoundException e) {

            log.warn("Can't resolve class name " + value, e);
            return String.class;
        }
    }

    @Override
    public @Nullable String toString(@NotNull Class<?> value) {
        return value.getName();
    }
}
