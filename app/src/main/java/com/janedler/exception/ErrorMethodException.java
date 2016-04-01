package com.janedler.exception;

public class ErrorMethodException extends RuntimeException {

    private static String msg = "方法调用错误";

    public ErrorMethodException(String detailMessage) {
        super(detailMessage);
        msg = detailMessage;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
