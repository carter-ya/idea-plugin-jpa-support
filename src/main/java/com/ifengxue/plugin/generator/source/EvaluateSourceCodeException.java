package com.ifengxue.plugin.generator.source;

public class EvaluateSourceCodeException extends RuntimeException {

    public EvaluateSourceCodeException() {
    }

    public EvaluateSourceCodeException(String message) {
        super(message);
    }

    public EvaluateSourceCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluateSourceCodeException(Throwable cause) {
        super(cause);
    }

    public EvaluateSourceCodeException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
