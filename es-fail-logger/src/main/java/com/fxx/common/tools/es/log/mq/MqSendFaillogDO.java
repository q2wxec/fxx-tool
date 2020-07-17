package com.fxx.common.tools.es.log.mq;

import com.fxx.common.tools.es.anotation.BaseEsModal;
import com.fxx.common.tools.es.anotation.EsModalId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
@BaseEsModal(index = "cmq_failinfo")
public class MqSendFaillogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @EsModalId
    private String id;

    /**
     * 应用名
     */
    private String applicationName;

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
     * 是否发送失败（1-发送失败，0-消费失败）
     */
    private Integer isSendFail;

    /**
     * 消息发送标签cmq-tag英文逗号分隔
     */

    private String msgTag;

    /**
     * 消息发送routingKey
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
     * 发送时间
     */

    private Date sendTime;
    /**
     * 异常信息
     */

    private String exceptionInfo;
    /**
     * 调用堆栈信息
     */

    private String stackInfo;
    /**
     * 补偿状态(1-未补偿，2-补偿成功，3-补偿失败)
     */

    private Integer compensateStatus;
    /**
     * 失败类型(1-超时，2-服务异常，3-调用失败,4-权限限制)
     */

    private Integer failType;
    /**
     * 补偿类型(1-需要补偿，2-不需要补偿)
     */

    private Integer compensateType;


    /**
     * 是否删除 0 否 1 是
     */


    private Integer deleted;
    /**
     * 创建时间
     */

    private Date createTime;
    /**
     * 更新时间
     */

    private Date updateTime;
    /**
     * 创建者
     */

    private String createBy;
    /**
     * 修改者
     */

    private String updateBy;

}
