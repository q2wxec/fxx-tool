package com.fxx.common.tools.excle;

import java.util.List;

import com.alibaba.excel.write.handler.WriteHandler;

import lombok.Data;

/**
 * @date 2020/5/7 14:11
 */
@Data
public class ExcelExportObject {
    /**
     * sheet名称
     */
    String       sheetName;
    /**
     * 导出自定义模板
     */
    Class        exportType;
    /**
     * 导出数据集合
     */
    List         dataList;
    /**
     * 导出sheet样式-覆盖主样式
     */
    WriteHandler writeHandler;
}
