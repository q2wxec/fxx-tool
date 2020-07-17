package com.fxx.common.tools.bean.print;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标识需要日志记录的字段以及中文标识
 *
 * @author wangxiao1
 * @date 2019/9/2515:45
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogLable {
    /**
     * 中文属性名称
     *
     * @return
     */
    String name();

    /**
     * 为true时，无论编辑前后结果是否变化，都进行记录
     *
     * @return
     */
    boolean alwaysLogWhenEdit() default false;
}
