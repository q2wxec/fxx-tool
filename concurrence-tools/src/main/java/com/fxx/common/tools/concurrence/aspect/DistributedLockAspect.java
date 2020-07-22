
package com.fxx.common.tools.concurrence.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.fxx.common.tools.concurrence.DistributorLockInter;
import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.exception.ToolException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2020/2/2517:43
 */
@Aspect
@Slf4j
public class DistributedLockAspect {

    private ExpressionParser                          parser                 = new SpelExpressionParser();
    private LocalVariableTableParameterNameDiscoverer discoverer             = new LocalVariableTableParameterNameDiscoverer();
    private static final String                       DISTRIBUTED_LOCK_PRFIX = "DISTRIBUTED_LOCK_PRFIX:";

    private DistributorLockInter                      distributorLock;

    public DistributedLockAspect(DistributorLockInter distributorLock) {
        this.distributorLock = distributorLock;
    }

    @Pointcut(value = "@annotation(com.fxx.common.tools.concurrence.aspect.JvDistributedLock)")
    public void distributedLock() {
    }

    @Around("distributedLock()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object obj;
        // 获取方法参数值
        Object[] arguments = joinPoint.getArgs();
        // 获取方法
        Method method = getMethod(joinPoint);
        String methodName = joinPoint.getTarget().getClass().toString() + "#" + method.getName();
        // 从注解中获取spel字符串
        JvDistributedLock distributedLock = method.getAnnotation(JvDistributedLock.class);
        String spel = distributedLock.key();
        boolean justByKey = distributedLock.justByKey();
        String keyPrfix = distributedLock.keyPrfix();
        long lockTimes = distributedLock.lockTimes();
        // 解析spel表达式
        Object keyObj = parseSpel(method, arguments, spel);
        ToolAssert.notNull(keyObj, "分布式锁获取key为null！");

        if (justByKey) {
            methodName = keyPrfix;
        }
        String key = DISTRIBUTED_LOCK_PRFIX + methodName + ":" + String.valueOf(keyObj);
        log.info("方法{}分布式加锁 key = [{}]", methodName, key);
        Object lockObj = distributorLock.getDistributorLock(key, lockTimes);
        if (lockObj != null) {
            log.info("方法{}分布式加锁  success, key = [{}]", methodName, key);
            try {
                Object retVal = null;
                Object[] args = joinPoint.getArgs();
                //方法调用
                retVal = joinPoint.proceed(args);
                return retVal;
            } finally {
                distributorLock.releaseLock(key, lockObj.toString());
                log.info("方法{}分布式锁释放  success, key = [{}]", methodName, key);
            }
        } else {
            // 获取锁失败，认为是重复提交的请求
            log.info("方法{}分布式加锁 fail, key = [{}]", methodName, key);
            throw new ToolException("您当前的请求可能存在重复或并发冲突，请您稍后重试，谢谢！详细信息：" + "方法：" + methodName + "，对应key" + key);
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(),
                        method.getParameterTypes());
            } catch (SecurityException | NoSuchMethodException e) {
                log.error("分布式锁，反射获取方法异常！");
                throw new RuntimeException(e);
            }
        }
        return method;
    }

    /**
     * 解析 spel 表达式
     *
     * @param method 方法
     * @param arguments 参数
     * @param spel 表达式
     * @return 执行spel表达式后的结果
     */
    private Object parseSpel(Method method, Object[] arguments, String spel) {
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], arguments[len]);
        }
        try {
            Expression expression = parser.parseExpression(spel);
            return expression.getValue(context);
        } catch (Exception e) {
            log.error("分布式锁，el表达式解析异常！");
            return null;
        }
    }

}
