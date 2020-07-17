
package com.fxx.common.tools.es.wrapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;

/**
 * @author wangxiao1
 * @date 2019/12/169:28
 */
@Data
public class BaseEsWrapper {
    protected Map<String, Object> termParams = new HashMap<>();
    protected Map<String, String> likeParams = new HashMap<>();
    protected Map<String, Object> lteParams = new HashMap<>();
    protected Map<String, Object> gteParams = new HashMap<>();
    protected Map<String, Object> ltParams = new HashMap<>();
    protected Map<String, Object> gtParams = new HashMap<>();
    protected Map<String, List> inParams = new HashMap<>();
    protected LinkedHashMap<String, SortOrder> sortParams = new LinkedHashMap<>();

    public static final String KEYWORD = ".keyword";
    public static final String KEYWORD_WILDCARD = "*";

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段将分词
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper eq(boolean condition, String field, Object param) {
        if (condition) {
            termParams.put(field, param);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段使用KEYWORD，不会分词
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper keq(boolean condition, String field, Object param) {
        if (condition) {
            termParams.put(field + KEYWORD, param);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段将分词
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper eq(String field, Object param) {
        return this.eq(true, field, param);
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段使用KEYWORD，不会分词
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper keq(String field, Object param) {
        return this.keq(true, field, param);
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段将分词
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param params
     * @return
     */
    public BaseEsWrapper in(boolean condition, String field, List params) {
        if (condition && CollectionUtil.isNotEmpty(params)) {
            inParams.put(field, params);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段使用KEYWORD，不会分词
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param params
     * @return
     */
    public BaseEsWrapper kIn(boolean condition, String field, List params) {
        if (condition && CollectionUtil.isNotEmpty(params)) {
            inParams.put(field + KEYWORD, params);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段将分词
     * </p>
     *
     * @param field  Java,Entity 属性，非数据库列名
     * @param params
     * @return
     */
    public BaseEsWrapper in(String field, List params) {
        return this.in(true, field, params);
    }

    /**
     * <p>
     * 等同于Entity 属性的"field=value"表达式，检索字段使用KEYWORD，不会分词
     * </p>
     *
     * @param field  Java,Entity 属性，非数据库列名
     * @param params
     * @return
     */
    public BaseEsWrapper kIn(String field, List params) {
        return this.kIn(true, field, params);
    }

    /**
     * <p>
     * LIKE条件语句，value中无需前后*，检索字段使用KEYWORD，不会分词
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名 字段名称
     * @param value     匹配值
     * @return this
     */
    public BaseEsWrapper klike(boolean condition, String field, String value) {
        if (condition && StrUtil.isNotBlank(field) && StrUtil.isNotBlank(value)) {
            likeParams.put(field + KEYWORD, KEYWORD_WILDCARD + value + KEYWORD_WILDCARD);
        }
        return this;
    }

    /**
     * <p>
     * LIKE条件语句，value中无需前后*，检索字段使用KEYWORD，不会分词
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名 字段名称
     * @param value 匹配值
     * @return this
     */
    public BaseEsWrapper klike(String field, String value) {
        return this.klike(true, field, value);
    }

    /**
     * <p>
     * LIKE条件语句，自定义通配符，检索字段将分词
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名 字段名称
     * @param value     匹配值
     * @return this
     */
    public BaseEsWrapper like(boolean condition, String field, String value) {
        if (condition && StrUtil.isNotBlank(field) && StrUtil.isNotBlank(value)) {
            likeParams.put(field, value);
        }
        return this;
    }

    /**
     * <p>
     * LIKE条件语句，自定义通配符，检索字段将分词
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名 字段名称
     * @param value 匹配值
     * @return this
     */
    public BaseEsWrapper like(String field, String value) {
        return this.like(true, field, value);
    }

    /**
     * <p>
     * 等同于Entity 属性的"field<=value"表达式
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper le(boolean condition, String field, Object param) {
        if (condition && StrUtil.isNotBlank(field) && param != null) {
            param = changeDateToTimeStamp(param);
            lteParams.put(field, param);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field<=value"表达式
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper le(String field, Object param) {
        return this.le(true, field, param);
    }

    /**
     * <p>
     * 等同于Entity 属性的"field>=value"表达式
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper ge(boolean condition, String field, Object param) {
        if (condition && StrUtil.isNotBlank(field) && param != null) {
            param = changeDateToTimeStamp(param);
            gteParams.put(field, param);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field>=value"表达式
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper ge(String field, Object param) {
        return this.ge(true, field, param);
    }


    /**
     * <p>
     * 等同于Entity 属性的"field<value"表达式
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper lt(boolean condition, String field, Object param) {
        if (condition && StrUtil.isNotBlank(field) && param != null) {
            param = changeDateToTimeStamp(param);
            ltParams.put(field, param);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field<value"表达式
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper lt(String field, Object param) {
        return this.lt(true, field, param);
    }

    /**
     * <p>
     * 等同于Entity 属性的"field>value"表达式
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper gt(boolean condition, String field, Object param) {
        if (condition && StrUtil.isNotBlank(field) && param != null) {
            param = changeDateToTimeStamp(param);
            gtParams.put(field, param);
        }
        return this;
    }

    /**
     * <p>
     * 等同于Entity 属性的"field>value"表达式
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名
     * @param param
     * @return
     */
    public BaseEsWrapper gt(String field, Object param) {
        return this.gt(true, field, param);
    }


    /**
     * <p>
     * SQL中orderby关键字跟的条件语句，可根据变更动态排序
     * </p>
     *
     * @param condition 拼接的前置条件
     * @param field     Java,Entity 属性，非数据库列名 SQL 中的 order by 语句，无需输入 Order By 关键字
     * @param isAsc     是否为升序
     * @return this
     */
    public BaseEsWrapper orderBy(boolean condition, String field, boolean isAsc) {
        SortOrder sortOrder = SortOrder.DESC;
        if (isAsc) {
            sortOrder = SortOrder.ASC;
        }
        if (condition && StrUtil.isNotBlank(field)) {
            sortParams.put(field, sortOrder);
        }
        return this;
    }

    /**
     * <p>
     * SQL中orderby关键字跟的条件语句，可根据变更动态排序
     * </p>
     *
     * @param field Java,Entity 属性，非数据库列名 SQL 中的 order by 语句，无需输入 Order By 关键字
     * @param isAsc 是否为升序
     * @return this
     */
    public BaseEsWrapper orderBy(String field, boolean isAsc) {
        return this.orderBy(true, field, isAsc);
    }

    private Object changeDateToTimeStamp(Object value) {
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        return value;
    }
}
