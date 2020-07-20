package com.fxx.common.tools.retry;

import com.fxx.common.tools.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2020/7/17
 */
@Slf4j
public class DefaultMethodFailLogRecorder implements MethodFailLogRecorder {
    /**
     * 方法异常日志记录
     *
     * @param methodFailInfo
     */
    @Override
    public void recordMethodFailLog(MethodFailInfo methodFailInfo) {
        log.error("DefaultMethodFailLogRecorder:" + JsonUtils.toJSONString(methodFailInfo));
    }
}
