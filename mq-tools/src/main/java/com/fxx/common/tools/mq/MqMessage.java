
package com.fxx.common.tools.mq;

import com.fxx.common.tools.utils.StrUtils;
import com.fxx.common.tools.utils.UUIDUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author wangxiao1
 * @date 2019/10/1716:39
 */
@Data
@Accessors(chain = true)
public class MqMessage {

    public static final String REQUEST_ID = "requestId";
    private String messageId;
    private Object data;
    private String requestId;

    public MqMessage(Object data) {
        this.messageId = UUIDUtils.randomUUID() + System.currentTimeMillis();
        this.data = data;
        String rid = MDC.get(REQUEST_ID);
        if (StrUtils.isBlank(rid)) {
            rid = UUID.randomUUID().toString().replace("-", "");
        }
        this.requestId = rid;
    }
}
