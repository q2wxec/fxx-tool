package com.fxx.common.tools.es.log.method;

import java.io.Serializable;
import java.util.Date;

import com.fxx.common.tools.es.anotation.BaseEsModal;
import com.fxx.common.tools.es.anotation.EsModalId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * SC调用失败日志记录
 * </p>
 *
 * @author wangxiao
 * @since 2019-09-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@BaseEsModal(index = "method_fail_info")
public class MethodFailLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @EsModalId
    private String            id;

    private String            applicationName;

    /**
     * 调用目标type
     */
    private String            targetClass;
    /**
     * 调用方法method
     */
    private String            methodName;

    /**
     * 调用参数
     */
    private String            methodParams;

    /**
     * 调用时间
     */
    private Date              requestTime;

    /**
     * 异常信息
     */
    private String            exceptionInfo;
    /**
     * 调用堆栈信息
     */
    private String            stackInfo;
    /**
     * 补偿状态(1-未补偿，2-补偿成功，3-补偿失败)
     */
    private Integer           compensateStatus;

    /**
     * 补偿类型(1-需要补偿，2-不需要补偿)
     */
    private Integer           compensateType;

    /**
     * 是否删除 0 否 1 是
     */
    private Integer           deleted;
    /**
     * 创建时间
     */
    private Date              createdTime;
    /**
     * 更新时间
     */
    private Date              updatedTime;
    /**
     * 创建者
     */
    private String            createBy;
    /**
     * 修改者
     */
    private String            updateBy;
}
