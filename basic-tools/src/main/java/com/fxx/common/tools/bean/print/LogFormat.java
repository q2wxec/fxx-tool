package com.fxx.common.tools.bean.print;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangxiao1
 * @date 2019/9/2615:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogFormat {
    Class<? extends LogFieldFormatInter> formatInter();
}
