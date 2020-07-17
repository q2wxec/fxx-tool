
package com.fxx.common.tools.status;

/**
 * @author wangxiao1
 * @date 2019/10/815:45
 */
public enum CompensateStatusEnum {
    NOT(1, "未补偿"),
    SUCCESS(2, "补偿成功"),
    FAIL(3, "补偿失败"),
    ;

    /**
     * code
     */
    private Integer code;
    /**
     * name
     */
    private String desc;

    CompensateStatusEnum(Integer code, String desc) {
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
