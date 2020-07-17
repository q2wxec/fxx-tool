
package com.fxx.common.tools.es.modal;

import lombok.Data;

/**
 * @author wangxiao1
 * @date 2019/12/1614:04
 */
@Data
public class ScrollInfo<A> extends PageInfo<A> {

    //滚动分页游标
    private String scrollId;

    private boolean scrollEnd = Boolean.FALSE;

}
