package com.fxx.common.tools.bean.print;

import java.lang.annotation.*;

/**
 * 用于标识列表对比唯一标识符
 *
 * @author wangxiao1
 * @date 2019/9/2516:51
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogIdForListCompare {
    /**
     * 用于比对的唯一标识字段
     *
     * @return
     */
    String idField();
}
