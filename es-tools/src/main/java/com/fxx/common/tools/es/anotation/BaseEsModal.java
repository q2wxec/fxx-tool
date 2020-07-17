package com.fxx.common.tools.es.anotation;


import java.lang.annotation.*;

/**
 * @author wangxiao1
 * @date 2019/12/129:15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface BaseEsModal {

    String index();

    //String type();

    short shards() default 5;

    short replicas() default 1;

    String refreshInterval() default "1s";

    String indexStoreType() default "fs";

    boolean createIndex() default true;


}
