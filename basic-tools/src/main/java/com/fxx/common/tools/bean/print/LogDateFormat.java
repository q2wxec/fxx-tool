package com.fxx.common.tools.bean.print;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于日志展示日期类型format
 *
 * @author wangxiao1
 * @date 2019/9/2515:45
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogDateFormat {

    String format() default "yyyy-MM-dd";
}
