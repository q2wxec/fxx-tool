package com.fxx.common.tools.mq;

import java.util.List;

/**
 * @author wangxiao1
 * @date 2020/7/16
 */
public interface MqFailRetryInter {

    default List<MqSendFaillogInfo> getList(MqSendFaillogInfo mqSendFaillogInfo, int page, int size) {
        return null;
    };

    default Long getSize(MqSendFaillogInfo cmqSendFaillogDO) {
        return null;
    };

    default Boolean cmqRetry(MqSendFaillogInfo failInfo) {
        return null;
    }
}
