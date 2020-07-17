
package com.fxx.common.tools.es.wrapper;

import lombok.Data;

/**
 * @author wangxiao1
 * @date 2019/12/1611:15
 */
@Data
public class EsEntityWrapper<A> extends AggregationEsWrapper {

    private Class<A> entityClass;

    public EsEntityWrapper(Class<A> entityClass) {
        this.entityClass = entityClass;
    }
}
