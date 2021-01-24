package com.ifengxue.plugin.util;

import lombok.Getter;

public class NoMatchTypeException extends RuntimeException {

    @Getter
    private final String dbType;

    public NoMatchTypeException(String dbType) {
        super("No match java type for db type " + dbType);
        this.dbType = dbType;
    }
}
