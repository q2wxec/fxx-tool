
package com.fxx.common.tools.es.log.mq;

import java.util.Date;
import java.util.Map;

import com.fxx.common.tools.es.CommonEsClient;
import com.fxx.common.tools.mq.MqFailEventRecorderInter;
import com.fxx.common.tools.mq.MqSendFaillogInfo;
import com.fxx.common.tools.status.CompensateStatusEnum;
import com.fxx.common.tools.utils.BeanUtils;
import com.fxx.common.tools.utils.ExceptionUtils;
import com.fxx.common.tools.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2019/10/1717:10
 */
@Slf4j
public class MqSendFaillogEsRecorder implements MqFailEventRecorderInter {

    private CommonEsClient commonEsClient;
    private String applicationName;

    public MqSendFaillogEsRecorder(CommonEsClient commonEsClient, String applicationName) {
        this.commonEsClient = commonEsClient;
        this.applicationName = applicationName;
    }

    @Override
    public void publishMqFailEvent(Object source, MqSendFaillogInfo mqSendFaillogInfo,
                                   Map<String, Object> mqServerParams) {
            MqSendFaillogDO mqSendFaillogDO = new MqSendFaillogDO();
            Exception exception = mqSendFaillogInfo.getException();
            BeanUtils.copyProperties(mqSendFaillogInfo, mqSendFaillogDO);
            mqSendFaillogDO.setSendTime(new Date());
            mqSendFaillogDO.setCompensateStatus(CompensateStatusEnum.NOT.getCode());
            mqSendFaillogDO.setApplicationName(applicationName);
            if (exception != null) {
                mqSendFaillogDO.setExceptionInfo(exception.toString());
                mqSendFaillogDO.setStackInfo(ExceptionUtils.stacktraceToString(exception));
            }
            try {
                commonEsClient.add(mqSendFaillogDO);
            } catch (Exception e) {
                log.error("保存mq消息发送失败日志异常，失败日志详细信息为：" + JsonUtils.toJSONString(mqSendFaillogDO), e);
            }
    }
}
