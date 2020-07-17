
package com.fxx.common.tools.es.modal;

import lombok.Data;

/**
 * @author wangxiao1
 * @date 2019/12/139:52
 */

@Data
public class EsBaseProperties {

    private String id;
    private String index;
    private String type;
    private short shards;
    private short replicas;
    private String refreshInterval;
    private String indexStoreType;
    private boolean createIndex;

    public static final String INDEX_SEPARTOR = "-";

    private String indexPrfix;


    public String getType() {
        return index;
    }

    public String getIndex() {
        String formateIndex = index;
        formateIndex = indexPrfix + INDEX_SEPARTOR + formateIndex;
        return formateIndex;
    }

    @Override
    public String toString() {
        return "EsBaseProperties{" +
                "id='" + id + '\'' +
                ", index='" + index + '\'' +
                ", type='" + index + '\'' +
                '}';
    }

}
