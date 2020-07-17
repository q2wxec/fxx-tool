package com.fxx.common.tools.mq;

public class MqSendFailException extends RuntimeException {
    public MqSendFailException() {
        super();
    }

    public MqSendFailException(String message) {
        super(message);
    }

    public MqSendFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
