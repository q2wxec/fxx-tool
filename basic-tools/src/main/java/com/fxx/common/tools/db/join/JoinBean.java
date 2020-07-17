package com.fxx.common.tools.db.join;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.utils.BeanUtils;
import com.fxx.common.tools.utils.CollUtils;

/**
 * @author wangxiao1
 * @date 2020/7/14
 */
public class JoinBean {

    /**
     * 主数据
     */
    private List<Object>        data;
    /**
     *
     */
    private Map<Object, Object> dataMap;
    /**
     * 主数据关联字段
     */
    private String              mainColumn;

    /**
     * 自身数据关联字段
     */
    private String              selfColumn;
    /**
     * 是否严格关联，为 true 情况的话 当关联数据为空报错
     */
    private Boolean             strictJoin = Boolean.FALSE;

    public JoinBean(List<Object> data, String mainColumn, String selfColumn) {
        this.data = data;
        this.mainColumn = mainColumn;
        this.selfColumn = selfColumn;
        dataMap = new HashMap<>();
        if (CollUtils.isNotEmpty(data)) {
            data.stream().forEach(o -> {
                Object fieldValue = BeanUtils.getFieldValue(o, selfColumn);
                ToolAssert.notNull(fieldValue, "数据关联字段不允许为空值！");
                dataMap.put(fieldValue, o);
            });
        }
    }

    public JoinBean(List<Object> data, String mainColumn, String selfColumn, Boolean strictJoin) {
        this(data, mainColumn, selfColumn);
        this.strictJoin = strictJoin;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public Map<Object, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<Object, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public String getMainColumn() {
        return mainColumn;
    }

    public void setMainColumn(String mainColumn) {
        this.mainColumn = mainColumn;
    }

    public String getSelfColumn() {
        return selfColumn;
    }

    public void setSelfColumn(String selfColumn) {
        this.selfColumn = selfColumn;
    }

    public Boolean getStrictJoin() {
        return strictJoin;
    }

    public void setStrictJoin(Boolean strictJoin) {
        this.strictJoin = strictJoin;
    }
}
