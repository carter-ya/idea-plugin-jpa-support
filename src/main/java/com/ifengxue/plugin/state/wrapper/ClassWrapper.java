package com.ifengxue.plugin.state.wrapper;

import com.ifengxue.plugin.state.converter.ClassConverter;
import com.intellij.util.xmlb.annotations.OptionTag;
import lombok.Data;

@Data
public class ClassWrapper {

    @OptionTag(converter = ClassConverter.class)
    private Class<?> clazz;

    public ClassWrapper() {
    }

    public ClassWrapper(Class<?> clazz) {
        this.clazz = clazz;
    }
}
