package com.fxx.common.tools.es.log.method;

import java.util.Date;

import com.fxx.common.tools.es.CommonEsClient;
import com.fxx.common.tools.retry.MethodFailInfo;
import com.fxx.common.tools.retry.MethodFailLogRecorder;
import com.fxx.common.tools.status.CompensateStatusEnum;
import com.fxx.common.tools.utils.BeanUtils;
import com.fxx.common.tools.utils.ExceptionUtils;
import com.fxx.common.tools.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2020/7/17
 */
@Slf4j
public class MethodFailLogEsRecorder implements MethodFailLogRecorder {

    private CommonEsClient commonEsClient;
    private String         applicationName;

    public MethodFailLogEsRecorder(CommonEsClient commonEsClient, String applicationName) {
        this.commonEsClient = commonEsClient;
        this.applicationName = applicationName;
    }

    /**
     * 方法异常日志记录
     *
     * @param methodFailInfo
     */
    //todo 异步化
    @Override
    public void recordMethodFailLog(MethodFailInfo methodFailInfo) {
        MethodFailLogDO methodFailLogDO = new MethodFailLogDO();
        Throwable exception = methodFailInfo.getException();
        BeanUtils.copyProperties(methodFailInfo, methodFailLogDO);
        methodFailLogDO.setTargetClass(methodFailInfo.getTargetClass().getName());
        methodFailLogDO.setCreatedTime(new Date());
        methodFailLogDO.setCompensateStatus(CompensateStatusEnum.NOT.getCode());
        methodFailLogDO.setApplicationName(applicationName);
        if (exception != null) {
            methodFailLogDO.setExceptionInfo(exception.toString());
            methodFailLogDO.setStackInfo(ExceptionUtils.stacktraceToString(exception));
        }
        try {
            commonEsClient.add(methodFailLogDO);
        } catch (Exception e) {
            log.error("保存方法失败日志异常，失败日志详细信息为：" + JsonUtils.toJSONString(methodFailLogDO), e);
        }
    }
}
