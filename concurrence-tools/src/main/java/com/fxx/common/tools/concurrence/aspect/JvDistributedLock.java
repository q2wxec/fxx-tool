package com.fxx.common.tools.concurrence.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangxiao1
 * @date 2020/2/2517:35 分布式锁注解
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JvDistributedLock {
    /**
     * 用于分布式加锁的key可使用EL表达式
     *
     * @return
     */
    String key();

    /**
     * 用于分布式加锁的时间 秒
     *
     * @return
     */
    long lockTimes() default 60L;

    boolean justByKey() default false;

    String keyPrfix() default "";
}
