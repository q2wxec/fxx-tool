
package com.fxx.common.tools.retry;

import java.io.Serializable;

import com.fxx.common.tools.status.CompensateTypeEnum;

/**
 * @author wangxiao1
 * @date 2019/9/2916:42 切记clientClass请通过类.class获取，使用对象获取将获取到代理类，而无法获取注解信息
 */
public class MethodFailInfo implements Serializable {

    private Class     targetClass;
    private String    methodName;
    private Object[]  methodParams;
    private Throwable exception;

    private String    methodUnique;
    private String    methodTag;

    private int       compensateType;

    public String getMethodUnique() {
        return methodUnique;
    }

    public void setMethodUnique(String methodUnique) {
        this.methodUnique = methodUnique;
    }

    public String getMethodTag() {
        return methodTag;
    }

    public void setMethodTag(String methodTag) {
        this.methodTag = methodTag;
    }

    /**
     * @param targetClass 切记clientClass请通过类.class获取，使用对象获取将获取到代理类，而无法获取注解信息
     * @param methodName 调用方法名称
     * @param exception 异常
     * @param requestParams 调用参数数组
     */
    public MethodFailInfo(Class targetClass, String methodName, Throwable exception, Object[] requestParams) {
        this.exception = exception;
        this.methodParams = requestParams;
        this.methodName = methodName;
        this.targetClass = targetClass;
        this.compensateType = CompensateTypeEnum.NEED.getCode();
    }

    /**
     * @param targetClass 切记clientClass请通过类.class获取，使用对象获取将获取到代理类，而无法获取注解信息
     * @param methodName 调用方法名称
     * @param requestParams 调用参数数组
     * @param returnMsg 返回消息
     */
    public MethodFailInfo(Class targetClass, String methodName, Object[] requestParams, Object returnMsg) {
        this.targetClass = targetClass;
        this.methodName = methodName;
        this.methodParams = requestParams;
        this.compensateType = CompensateTypeEnum.NEED.getCode();
    }

    /**
     * 默认失败需要补偿，当该远程调用失败，但无需后续补偿重发，调用此方法设置
     */
    public void setCompensateTypeNotNeed() {
        this.compensateType = CompensateTypeEnum.NOTNEED.getCode();
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(Object[] methodParams) {
        this.methodParams = methodParams;
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
