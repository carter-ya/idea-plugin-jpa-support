package com.ifengxue.plugin.util;

import com.google.common.collect.Lists;
import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.intellij.openapi.ui.Messages;
import java.util.List;

public final class TypeUtil {

    public static List<String> getAllJavaDbType() {
        return Lists.newArrayList(
            byte.class.getName(),
            Byte.class.getName(),
            "byte[]",
            boolean.class.getName(),
            Boolean.class.getName(),
            char.class.getName(),
            Character.class.getName(),
            short.class.getName(),
            Short.class.getName(),
            int.class.getName(),
            Integer.class.getName(),
            long.class.getName(),
            Long.class.getName(),
            float.class.getName(),
            Float.class.getName(),
            double.class.getName(),
            Double.class.getName(),
            String.class.getName(),
            java.util.Date.class.getName(),
            java.sql.Blob.class.getName(),
            java.sql.Clob.class.getName(),
            java.sql.Date.class.getName(),
            java.sql.Time.class.getName(),
            java.sql.Timestamp.class.getName(),
            java.sql.Array.class.getName(),
            java.time.LocalTime.class.getName(),
            java.time.LocalDate.class.getName(),
            java.time.LocalDateTime.class.getName(),
            java.time.ZonedDateTime.class.getName()
        );
    }

    public static Class<?> javaDbTypeToClassAndShowDialogOnError(String type) {
        try {
            return javaDbTypeToClass(type);
        } catch (Exception e) {
            Messages.showErrorDialog(LocaleContextHolder.format("invalid_type", type), Constants.PLUGIN_NAME);
            return null;
        }
    }

    public static Class<?> javaDbTypeToClass(String type) throws ClassNotFoundException {
        Class<?> clazz;
        switch (type) {
            case "byte":
                clazz = byte.class;
                break;
            case "char":
                clazz = char.class;
                break;
            case "short":
                clazz = short.class;
                break;
            case "int":
                clazz = int.class;
                break;
            case "long":
                clazz = long.class;
                break;
            case "float":
                clazz = float.class;
                break;
            case "double":
                clazz = double.class;
                break;
            case "boolean":
                clazz = boolean.class;
                break;
            case "byte[]":
                clazz = byte[].class;
                break;
            default:
                clazz = Class.forName(type);
        }
        return clazz;
    }

    public static String javaDbTypeToString(Class<?> type) {
        if (type == byte[].class) {
            return "byte[]";
        }
        return type.getName();
    }
}
