package com.fxx.common.tools.exception;

/**
 * @author wangxiao1
 * @date 2020/6/12
 */
public class ToolException extends RuntimeException {
    public ToolException() {
        super();
    }

    public ToolException(String message) {
        super(message);
    }

    public ToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
