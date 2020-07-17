
package com.fxx.common.tools.bean.print;

/**
 * @author wangxiao1
 * @date 2019/9/2515:59
 */
public class UpdateLogResult {
    private String before;
    private String after;

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public UpdateLogResult(String before, String after) {
        this.before = before;
        this.after = after;
    }

    public UpdateLogResult() {
    }
}
