
package com.fxx.common.tools.retry;

import com.fxx.common.tools.status.CompensateTypeEnum;

import java.io.Serializable;

/**
 * @author wangxiao1
 * @date 2019/9/2916:42 切记clientClass请通过类.class获取，使用对象获取将获取到代理类，而无法获取注解信息
 */
public class MethodFailInfo implements Serializable {

    private Class     clientClass;
    private String    requestMethod;
    private Object[]  requestParams;
    private Throwable exception;
    private Object    returnMsg;
    private String    methodUnique;
    private String    methodTag;

    private int       compensateType;

    /**
     * @param clientClass 切记clientClass请通过类.class获取，使用对象获取将获取到代理类，而无法获取注解信息
     * @param requestMethod 调用方法名称
     * @param exception 异常
     * @param requestParams 调用参数数组
     */
    public MethodFailInfo(Class clientClass, String requestMethod, Throwable exception, Object[] requestParams) {
        this.exception = exception;
        this.requestParams = requestParams;
        this.requestMethod = requestMethod;
        this.clientClass = clientClass;
        this.compensateType = CompensateTypeEnum.NEED.getCode();
    }

    /**
     * @param clientClass 切记clientClass请通过类.class获取，使用对象获取将获取到代理类，而无法获取注解信息
     * @param requestMethod 调用方法名称
     * @param requestParams 调用参数数组
     * @param returnMsg 返回消息
     */
    public MethodFailInfo(Class clientClass, String requestMethod, Object[] requestParams, Object returnMsg) {
        this.clientClass = clientClass;
        this.requestMethod = requestMethod;
        this.requestParams = requestParams;
        this.returnMsg = returnMsg;
        this.compensateType = CompensateTypeEnum.NEED.getCode();
    }

    /**
     * 默认失败需要补偿，当该远程调用失败，但无需后续补偿重发，调用此方法设置
     */
    public void setCompensateTypeNotNeed() {
        this.compensateType = CompensateTypeEnum.NOTNEED.getCode();
    }

    public Object getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(Object returnMsg) {
        this.returnMsg = returnMsg;
    }

    public Class getClientClass() {
        return clientClass;
    }

    public void setClientClass(Class clientClass) {
        this.clientClass = clientClass;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Object[] getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Object[] requestParams) {
        this.requestParams = requestParams;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public int getCompensateType() {
        return compensateType;
    }
}
