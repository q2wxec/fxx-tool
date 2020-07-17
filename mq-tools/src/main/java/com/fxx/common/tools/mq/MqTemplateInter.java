
package com.fxx.common.tools.mq;

import java.util.List;

/**
 * @author wangxiao1
 * @date 2020/6/12
 */
public interface MqTemplateInter {

    String sendToTopic(String topicName, MqMessage message, List<String> tags, String routingKey) throws Exception;

    String sendToQueue(String queueName, MqMessage message, int delaySeconds) throws Exception;

    List<String> batchSendToTopic(String topicName, List<MqMessage> msgList, List<String> vTagList, String routingKey) throws Exception;

    List<String> batchSendToQueue(String queueName, List<MqMessage> msgList, int delaySeconds) throws Exception;
}
