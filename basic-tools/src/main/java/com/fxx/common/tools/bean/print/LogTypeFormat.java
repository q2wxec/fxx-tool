package com.fxx.common.tools.bean.print;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangxiao1
 * @date 2019/9/2617:49
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogTypeFormat {

    /**
     * 注解传入模式{"1,新建","2,更新"},
     * 间隔使用英文逗号
     *
     * @return
     */
    String[] logTypes();


}
