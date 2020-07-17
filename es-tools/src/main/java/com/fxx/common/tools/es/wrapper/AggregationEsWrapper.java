
package com.fxx.common.tools.es.wrapper;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;

/**
 * @author wangxiao1
 * @date 2019/12/2514:11
 */
@Data
public class AggregationEsWrapper extends BaseEsWrapper {


    public static final String MAX_PREFIX = "max_";
    public static final String MIN_PREFIX = "min_";
    public static final String AVG_PREFIX = "avg_";
    public static final String SUM_PREFIX = "sum_";
    public static final String COUNT = "count";

    private Map<String, AggregationTypeEnum> aggregationFileds = new HashMap<>();

    private Set<String> groupFileds = new HashSet<>();
    protected LinkedHashMap<String, Boolean> aggregationSortParams = new LinkedHashMap<>();

    public BaseEsWrapper max(String field) {
        if (StrUtil.isNotBlank(field)) {
            aggregationFileds.put(field, AggregationTypeEnum.MAX);
        }
        return this;
    }

    public BaseEsWrapper min(String field) {
        if (StrUtil.isNotBlank(field)) {
            aggregationFileds.put(field, AggregationTypeEnum.MIN);
        }
        return this;
    }

    public BaseEsWrapper sum(String field) {
        if (StrUtil.isNotBlank(field)) {
            aggregationFileds.put(field, AggregationTypeEnum.SUM);
        }
        return this;
    }

    public BaseEsWrapper avg(String field) {
        if (StrUtil.isNotBlank(field)) {
            aggregationFileds.put(field, AggregationTypeEnum.AVG);
        }
        return this;
    }

    public BaseEsWrapper max(String field, boolean isAsc) {
        max(field);
        aggregationSort(getMaxField(field), isAsc);
        return this;
    }

    public BaseEsWrapper min(String field, boolean isAsc) {
        min(field);
        aggregationSort(getMinField(field), isAsc);
        return this;
    }

    public BaseEsWrapper sum(String field, boolean isAsc) {
        sum(field);
        aggregationSort(getSumField(field), isAsc);
        return this;
    }

    public BaseEsWrapper avg(String field, boolean isAsc) {
        avg(field);
        aggregationSort(getAvgField(field), isAsc);
        return this;
    }

    public BaseEsWrapper groupBy(String field) {
        if (StrUtil.isNotBlank(field)) {
            groupFileds.add(field);
        }
        return this;
    }

    private BaseEsWrapper aggregationSort(String field, boolean isAsc) {
        SortOrder sortOrder = SortOrder.DESC;
        if (StrUtil.isNotBlank(field)) {
            aggregationSortParams.put(field, isAsc);
        }
        return this;
    }

    public static String getMaxField(String field) {
        if (StrUtil.isNotBlank(field)) {
            return MAX_PREFIX + field;
        }
        return null;
    }

    public static String getMinField(String field) {
        if (StrUtil.isNotBlank(field)) {
            return MIN_PREFIX + field;
        }
        return null;
    }

    public static String getSumField(String field) {
        if (StrUtil.isNotBlank(field)) {
            return SUM_PREFIX + field;
        }
        return null;
    }

    public static String getAvgField(String field) {
        if (StrUtil.isNotBlank(field)) {
            return AVG_PREFIX + field;
        }
        return null;
    }
}


