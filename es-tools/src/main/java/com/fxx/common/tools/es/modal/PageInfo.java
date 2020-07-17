
package com.fxx.common.tools.es.modal;

import lombok.Data;

import java.util.List;

/**
 * @author wangxiao1
 * @date 2019/12/1614:04
 */
@Data
public class PageInfo<A> {

    //总记录数
    private long total;
    //结果集
    private List<A> list;
    //当前页
    private int pageNum;
    //每页的数量
    private int pageSize;

}
