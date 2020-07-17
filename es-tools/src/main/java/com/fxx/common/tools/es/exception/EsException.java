package com.fxx.common.tools.es.exception;

/**
 * @author wangxiao1
 * @date 2020/6/12
 */
public class EsException extends RuntimeException {

    public EsException() {
        super();
    }

    public EsException(String message) {
        super(message);
    }

    public EsException(String message, Throwable cause) {
        super(message, cause);
    }
}
