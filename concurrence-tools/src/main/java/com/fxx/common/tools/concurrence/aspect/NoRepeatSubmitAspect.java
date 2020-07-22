package com.fxx.common.tools.concurrence.aspect;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fxx.common.tools.concurrence.DistributorLockInter;
import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.exception.ToolException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Aspect
@Slf4j
public class NoRepeatSubmitAspect {

    public static final String   AUTHORIZATION = "Authorization";

    private DistributorLockInter distributorLock;

    public NoRepeatSubmitAspect(DistributorLockInter distributorLock) {
        this.distributorLock = distributorLock;
    }

    @Pointcut("@annotation(noRepeatSubmit)")
    public void pointCut(NoRepeatSubmit noRepeatSubmit) {
    }

    @Around("pointCut(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint pjp, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        int lockSeconds = noRepeatSubmit.lockTime();
        String url = pjp.getTarget().getClass().toString() + "_" + pjp.getSignature().getName();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        ToolAssert.notNull(attributes, "NoRepeatSubmitAspect ServletRequestAttributes 为空");

        HttpServletRequest request = attributes.getRequest();
        ToolAssert.notNull(request, "NoRepeatSubmitAspect HttpServletRequest 为空");

        String token = request.getHeader(AUTHORIZATION);
        ToolAssert.hasText(token, "NoRepeatSubmitAspect token 为空");

        String key = "NoRepeatSubmitAspect_tryLock_" + url + "_" + token;
        Object obj = distributorLock.getDistributorLock(key, lockSeconds);
        log.info("tryLock key = [{}]", key);
        if (obj != null) {
            log.info("tryLock success, key = [{}]", key);
            // 获取锁成功
            Object result;
            try {
                // 执行进程
                result = pjp.proceed();
            } finally {
                //解锁
                distributorLock.releaseLock(key, obj.toString());
                log.info("releaseLock success, key = [{}]", key);
            }
            return result;
        } else {
            // 获取锁失败，认为是重复提交的请求
            log.info("tryLock fail, key = [{}]", key);
            throw new ToolException("TOO_MANY_REQUEST!");
        }
    }
}
