package com.fxx.common.tools.retry;

/**
 * @author wangxiao1
 * @date 2020/7/17
 */
public interface MethodFailLogRecorder {
    /**
     * 方法异常日志记录
     *
     * @param methodFailInfo
     */
    void recordMethodFailLog(MethodFailInfo methodFailInfo);
}
