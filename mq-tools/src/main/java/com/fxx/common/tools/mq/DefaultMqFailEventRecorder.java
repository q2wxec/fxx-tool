package com.fxx.common.tools.mq;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Slf4j
public class DefaultMqFailEventRecorder implements MqFailEventRecorderInter {

    @Override
    public void publishMqFailEvent(Object source, MqSendFaillogInfo mqSendFaillogInfo,
                                   Map<String, Object> mqServerParams) {
        Exception exception = mqSendFaillogInfo.getException();
        if (exception == null) {
            exception = new RuntimeException(String.valueOf(mqSendFaillogInfo));
        }
        throw new MqSendFailException(String.valueOf(mqSendFaillogInfo), exception);
    }
}
