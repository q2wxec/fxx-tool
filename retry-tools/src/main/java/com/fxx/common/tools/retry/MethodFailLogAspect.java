package com.fxx.common.tools.retry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2019/9/1813:58
 */
@Aspect
@Slf4j
public class MethodFailLogAspect {

    private MethodFailLogRecorder methodFailLogRecorder;

    public MethodFailLogAspect(MethodFailLogRecorder methodFailLogRecorder) {
        this.methodFailLogRecorder = methodFailLogRecorder;
    }

    @Pointcut(value = "@annotation(com.fxx.common.tools.retry.MethodFailLog)")
    public void log() {
    }

    /**
     * around:根据日志注解类型为方法调用记录日志 <br/>
     */
    @Around("log()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object retVal = null;
        Object[] args = joinPoint.getArgs();
        //方法调用
        try {
            retVal = joinPoint.proceed(args);
        } catch (Exception e) {
            record(joinPoint, e);
        }
        return retVal;
    }

    private void record(ProceedingJoinPoint joinPoint, Exception e) {
        MethodFailLog methodFailLog = ((MethodSignature) joinPoint.getSignature()).getMethod()
                .getAnnotation(MethodFailLog.class);
        String methodTag = methodFailLog.methodTag();
        String methodUnique = methodFailLog.methodUnique();
        //获取代理原始接口或类
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        //获取方法签名
        String methodName = joinPoint.getSignature().getName();
        MethodFailInfo failInfo = new MethodFailInfo(target.getClass(), methodName, e, args);
        failInfo.setMethodTag(methodTag);
        failInfo.setMethodUnique(methodUnique);
        methodFailLogRecorder.recordMethodFailLog(failInfo);
    }

}
