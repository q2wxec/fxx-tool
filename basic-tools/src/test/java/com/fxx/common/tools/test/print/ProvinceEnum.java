
package com.fxx.common.tools.test.print;

import com.fxx.common.tools.bean.print.LogFieldFormatInter;

/**
 * @date 2019/9/1618:44
 */
public enum ProvinceEnum implements LogFieldFormatInter {
    WUHAN(1, "武汉"),
    SHANGHAI(2, "上海");

    /**
     * code
     */
    private Integer code;
    /**
     * name
     */
    private String desc;

    ProvinceEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static String getByCode(Integer code) {
        ProvinceEnum[] provinceEnums = ProvinceEnum.values();
        for (ProvinceEnum provinceEnum : provinceEnums) {
            if (provinceEnum.getCode().equals(code)) {
                return provinceEnum.getDesc();
            }
        }
        return null;
    }

    public String format(Object obj) {
        return ProvinceEnum.getByCode((Integer) obj);
    }
}
