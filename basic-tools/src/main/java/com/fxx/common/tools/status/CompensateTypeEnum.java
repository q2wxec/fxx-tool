
package com.fxx.common.tools.status;

/**
 * @author wangxiao1
 * @date 2019/10/815:44
 */
public enum CompensateTypeEnum {
    NEED(1, "需要补偿"),
    NOTNEED(2, "不需要补偿"),
    ;


    /**
     * code
     */
    private Integer code;
    /**
     * name
     */
    private String desc;

    CompensateTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
