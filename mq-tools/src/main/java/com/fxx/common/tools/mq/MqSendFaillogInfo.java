package com.fxx.common.tools.mq;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * cmq消息发送失败日志记录
 * </p>
 *
 * @author wangxiao1
 * @since 2019-10-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MqSendFaillogInfo {

    private String    id;

    /**
     * 消息发送主题/队列，cmq-topic/queue，基于消息类型区分
     */
    private String msgTarget;
    /**
     * 消息类型（1-队列,2-主题）
     */
    private Integer msgType;
    /**
     * 消息类型（1-单条,2-批量）
     */
    private Integer sendType;
    /**
     * 消息发送标签cmq-tag英文逗号分隔
     */
    private String msgTag;
    /**
     * routingKey
     */
    private String routingKey;
    /**
     * 自定义，消息唯一标识用于幂等判断
     */
    private String msgId;
    /**
     * 消息生效延迟时间
     */
    private Integer msgDelay;
    /**
     * 消息内容
     */
    private String mqMsg;
    /**
     * 失败异常
     */
    private Exception exception;

    /**
     * 补偿类型(1-需要补偿，2-不需要补偿)
     */
    private Integer compensateType;


    /**
     * 是否发送失败（1-发送失败，0-消费失败）
     */
    private Integer isSendFail;

    public MqSendFaillogInfo(String msgTarget, Integer msgType, Integer sendType, String msgTag, String routingKey, String msgId, Integer msgDelay, String mqMsg, Exception exception, Integer compensateType, Integer isSendFail) {
        this.msgTarget = msgTarget;
        this.sendType = sendType;
        this.msgType = msgType;
        this.msgTag = msgTag;
        this.msgId = msgId;
        this.msgDelay = msgDelay;
        this.mqMsg = mqMsg;
        this.exception = exception;
        this.compensateType = compensateType;
        this.routingKey = routingKey;
        this.isSendFail = isSendFail;
    }

    @Override
    public String toString() {
        return "MqSendFaillogInfo{" +
                "msgTarget='" + msgTarget + '\'' +
                ", msgType=" + msgType +
                ", sendType=" + sendType +
                ", msgTag='" + msgTag + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", msgId='" + msgId + '\'' +
                ", msgDelay=" + msgDelay +
                ", mqMsg='" + mqMsg + '\'' +
                ", compensateType=" + compensateType +
                ", isSendFail=" + isSendFail +
                '}';
    }
}
