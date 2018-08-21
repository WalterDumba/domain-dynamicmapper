package com.dynamicmapper.exceptions;

public class DeepCopyTypesMissMatchException extends RuntimeException {




    public DeepCopyTypesMissMatchException(String message) {
        super(message);
    }
    public DeepCopyTypesMissMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
