package com.fxx.common.tools.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangxiao1
 * @date 2019/9/2617:49
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodFailLog {
    /**
     * 用于重名方法情况方法的唯一标识
     *
     * @return
     */
    String methodUnique() default "";

    /**
     * 用于方法描述
     *
     * @return
     */
    String methodTag() default "";

}
