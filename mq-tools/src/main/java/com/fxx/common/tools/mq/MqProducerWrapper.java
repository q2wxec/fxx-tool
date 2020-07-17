
package com.fxx.common.tools.mq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.status.CompensateTypeEnum;
import com.fxx.common.tools.utils.CollUtils;
import com.fxx.common.tools.utils.HashUtils;
import com.fxx.common.tools.utils.JsonUtils;
import com.fxx.common.tools.utils.StrUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2019/10/1615:30
 */

@Slf4j
public class MqProducerWrapper {

    private MqTemplateInter mqTemplate;

    private MqFailEventRecorderInter mqFailEventRecorderInter;

    private Map<String, Object>      mqServerParams;

    private Executor                 executor;

    public MqProducerWrapper(MqTemplateInter mqTemplate, MqFailEventRecorderInter mqFailEventRecorderInter,
                             Map<String, Object> mqServerParams, Executor executor) {
        this.mqTemplate = mqTemplate;
        this.mqFailEventRecorderInter = mqFailEventRecorderInter;
        this.mqServerParams = mqServerParams;
        this.executor = executor;
    }

    public static final Integer QUEUE_MSG_TYPE = 1;
    public static final Integer TOPIC_MSG_TYPE = 2;
    public static final Integer MSG_SEND_SINGLE = 1;
    public static final Integer MSG_SEND_BATCH = 2;
    public static final boolean MSG_COMPENSATION_ON = true;

    /**
     * @param topic   主题名
     * @param message 消息
     *                默认启用消息补偿机制不抛出异常
     * @return
     */
    public String sendToTopic(String topic, MqMessage message) {
        return this.sendToTopic(topic, message, null, null, MSG_COMPENSATION_ON);
    }

    /**
     * @param queueName 队列名
     * @param message   消息
     *                  默认启用消息补偿机制不抛出异常
     * @return
     */
    public String sendToQueue(String queueName, MqMessage message) {
        return this.sendToQueue(queueName, message, 0, MSG_COMPENSATION_ON);
    }

    /**
     * @param topicName 主题名
     * @param message   消息体
     * @param tags      消息标签列表
     *                  默认启用消息补偿机制不抛出异常
     * @return 消息ID
     */
    public String sendToTopic(String topicName, MqMessage message, List<String> tags) {
        return this.sendToTopic(topicName, message, tags, MSG_COMPENSATION_ON);
    }

    /**
     * @param topicName       主题名
     * @param message         消息体
     * @param tags            消息标签列表
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息ID
     */
    public String sendToTopic(String topicName, MqMessage message, List<String> tags, boolean msgCompensation) {
        return this.sendToTopic(topicName, message, tags, null, msgCompensation);
    }

    /**
     * @param topicName       主题名
     * @param message         消息体
     * @param routingKey      路由key
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息ID
     */
    public String sendToTopic(String topicName, MqMessage message, String routingKey, boolean msgCompensation) {
        return this.sendToTopic(topicName, message, null, routingKey, msgCompensation);
    }

    /**
     * @param topicName       主题名
     * @param message         消息体
     * @param tags            消息标签列表
     * @param routingKey      路由key
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息ID
     */
    public String sendToTopic(String topicName, MqMessage message, List<String> tags, String routingKey, boolean msgCompensation) {
        String msgId = null;
        try {
            msgId = mqTemplate.sendToTopic(topicName, message, tags, routingKey);
        } catch (Exception e) {
            log.error("向mq主题:[" + topicName + "],发送消息[" + JsonUtils.toJSONString(message) + "]失败", e);
            logWhileMqException(topicName, JsonUtils.toJSONString(message), TOPIC_MSG_TYPE, MSG_SEND_SINGLE, tags, routingKey, null, mqTemplate, e, msgCompensation);
            //当启用消息补偿时，不抛出异常
            if (!msgCompensation) {
                throw new RuntimeException(e);
            }
        }
        return msgId;
    }

    /**
     * @param topicName  主题名
     * @param messageLst 消息体List
     * @param tags       消息标签列表
     *                   默认启用消息补偿机制不抛出异常
     * @return 消息IDs
     */
    public List<String> batchSendToTopic(String topicName, List<MqMessage> messageLst, List<String> tags) {
        return this.batchSendToTopic(topicName, messageLst, tags, MSG_COMPENSATION_ON);
    }


    /**
     * @param topicName       主题名
     * @param messageLst      消息体List
     * @param tags            消息标签列表
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息IDs
     */
    public List<String> batchSendToTopic(String topicName, List<MqMessage> messageLst, List<String> tags, boolean msgCompensation) {
        return this.batchSendToTopic(topicName, messageLst, tags, null, msgCompensation);
    }

    /**
     * @param topicName       主题名
     * @param messageLst      消息体List
     * @param routingKey      路由key
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息IDs
     */
    public List<String> batchSendToTopic(String topicName, List<MqMessage> messageLst, String routingKey, boolean msgCompensation) {
        return this.batchSendToTopic(topicName, messageLst, null, routingKey, msgCompensation);
    }

    /**
     * @param topicName       主题名
     * @param messageLst      消息体List
     * @param tags            消息标签列表
     * @param routingKey      路由key
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息IDs
     */
    public List<String> batchSendToTopic(String topicName, List<MqMessage> messageLst, List<String> tags, String routingKey, boolean msgCompensation) {
        List<String> msgIds = null;
        try {
            msgIds = mqTemplate.batchSendToTopic(topicName, messageLst, tags, routingKey);
        } catch (Exception e) {
            log.error("向mq主题:[" + topicName + "],批量发送消息[" + JsonUtils.toJSONString(messageLst) + "]失败", e);
            logWhileMqException(topicName, JsonUtils.toJSONString(messageLst), TOPIC_MSG_TYPE, MSG_SEND_BATCH, tags, routingKey, null, mqTemplate, e, msgCompensation);
            //当启用消息补偿时，不抛出异常
            if (!msgCompensation) {
                throw new RuntimeException(e);
            }
        }
        return msgIds;
    }

    /**
     * @param queueName    队列名
     * @param message      消息体
     * @param delaySeconds 消息激活延迟时间
     *                     默认启用消息补偿机制不抛出异常
     * @return 消息ID
     */
    public String sendToQueue(String queueName, MqMessage message, int delaySeconds) {
        return this.sendToQueue(queueName, message, delaySeconds, MSG_COMPENSATION_ON);
    }

    /**
     * @param queueName       队列名
     * @param message         消息体
     * @param delaySeconds    消息激活延迟时间
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息ID
     */
    public String sendToQueue(String queueName, MqMessage message, int delaySeconds, boolean msgCompensation) {
        String msgId = null;
        try {
            msgId = mqTemplate.sendToQueue(queueName, message, delaySeconds);
        } catch (Exception e) {
            log.error("向mq队列:[" + queueName + "],发送消息[" + JsonUtils.toJSONString(message) + "]失败", e);
            logWhileMqException(queueName, JsonUtils.toJSONString(message), QUEUE_MSG_TYPE, MSG_SEND_SINGLE, null, null, delaySeconds, mqTemplate, e, msgCompensation);
            //当启用消息补偿时，不抛出异常
            if (!msgCompensation) {
                throw new RuntimeException(e);
            }
        }
        return msgId;
    }

    public String sendByMqBean(MqSendFaillogInfo failInfo) {
        Integer msgType = failInfo.getMsgType();
        ToolAssert.notNull(msgType, "消息类型为null！请检查");
        String mqMsg = failInfo.getMqMsg();
        ToolAssert.hasText(mqMsg, "消息内容为空！请检查");
        MqMessage MqMessage = JsonUtils.toJavaObject(mqMsg, MqMessage.class);
        String msgTarget = failInfo.getMsgTarget();
        ToolAssert.hasText(msgTarget, "消息发送队列/主题为空！请检查");
        Integer msgDelay = failInfo.getMsgDelay();
        String result = null;
        if (QUEUE_MSG_TYPE.equals(msgType)) {
            result = this.sendToQueue(msgTarget, MqMessage, msgDelay, false);
        } else if (TOPIC_MSG_TYPE.equals(msgType)) {
            List tags = null;
            String msgTag = failInfo.getMsgTag();
            if (StrUtils.isNotBlank(msgTag)) {
                tags = JsonUtils.toList(msgTag);
            }
            String routingKey = failInfo.getRoutingKey();
            result = this.sendToTopic(msgTarget, MqMessage, tags, routingKey, false);
        }
        return result;
    }

    /**
     * @param queueName    队列名
     * @param messageLst   消息体List
     * @param delaySeconds 消息激活延迟时间
     *                     默认启用消息补偿机制不抛出异常
     * @return 消息IDs
     */
    public List<String> batchSendToQueue(String queueName, List<MqMessage> messageLst, int delaySeconds) {
        return this.batchSendToQueue(queueName, messageLst, delaySeconds, MSG_COMPENSATION_ON);
    }


    /**
     * @param queueName       队列名
     * @param messageLst      消息体List
     * @param delaySeconds    消息激活延迟时间
     * @param msgCompensation 是否启用消息补偿机制，启用则不抛出异常
     * @return 消息IDs
     */
    public List<String> batchSendToQueue(String queueName, List<MqMessage> messageLst, int delaySeconds, boolean msgCompensation) {
        List<String> msgIds = null;
        try {
            msgIds = mqTemplate.batchSendToQueue(queueName, messageLst, delaySeconds);
        } catch (Exception e) {
            log.error("向mq队列:[" + queueName + "],批量发送消息[" + JsonUtils.toJSONString(messageLst) + "]失败", e);
            logWhileMqException(queueName, JsonUtils.toJSONString(messageLst), QUEUE_MSG_TYPE, MSG_SEND_BATCH, null, null, delaySeconds, mqTemplate, e, msgCompensation);
            //当启用消息补偿时，不抛出异常
            if (!msgCompensation) {
                throw new RuntimeException(e);
            }
        }
        return msgIds;
    }

    private void logWhileMqException(String target, String message, int msgType, int sendType, List<String> tags, String routingKey, Integer delaySeconds, MqTemplateInter mqTemplate, Exception e, boolean msgCompensation) {
        String messageId = null;
        if (MSG_SEND_SINGLE.equals(sendType)) {
            messageId = JsonUtils.toJavaObject(message, MqMessage.class).getMessageId();
        } else {
            messageId = String.valueOf(HashUtils.apHash(message));
        }
        String tagsStr = null;
        if (tags != null) {
            tagsStr = JsonUtils.toJSONString(tags);
        }
        int compensateType = msgCompensation ? CompensateTypeEnum.NEED.getCode() : CompensateTypeEnum.NOTNEED.getCode();
        MqSendFaillogInfo mqSendFaillogInfo = new MqSendFaillogInfo(target, msgType, sendType, tagsStr, routingKey, messageId, delaySeconds, message, e, compensateType, 1);
        if (mqFailEventRecorderInter == null) {
            mqFailEventRecorderInter = new DefaultMqFailEventRecorder();
        }
        if (mqServerParams == null) {
            mqServerParams = CollUtils.newHashMap();
        }
        if (executor != null) {
            executor.execute(() -> {
                mqFailEventRecorderInter.publishMqFailEvent(this, mqSendFaillogInfo, mqServerParams);
            });
        } else {
            mqFailEventRecorderInter.publishMqFailEvent(this, mqSendFaillogInfo, mqServerParams);
        }
    }


}
