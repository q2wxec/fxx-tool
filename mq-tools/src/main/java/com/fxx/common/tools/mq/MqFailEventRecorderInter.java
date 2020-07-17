
package com.fxx.common.tools.mq;


import java.util.Map;

/**
 * @author wangxiao1
 * @date 2019/9/3015:32
 * <p>
 * 失败事件发布工具类
 */
public interface MqFailEventRecorderInter {

    /**
     * @param source            当前调用对象，用于构建事件时，指定消息来源
     * @param mqSendFaillogInfo 事件需要传递的信息封装
     * @param mqServerParams
     */
    void publishMqFailEvent(Object source, MqSendFaillogInfo mqSendFaillogInfo, Map<String, Object> mqServerParams);


}
