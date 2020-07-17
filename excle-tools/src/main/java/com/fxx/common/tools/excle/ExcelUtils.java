package com.fxx.common.tools.excle;

import java.io.File;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.HorizontalAlignment;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * excel导出，样式定义
 *
 * @date 2019/10/25 17:46
 */
@Slf4j
public class ExcelUtils {

    /**
     * 设置样式为水平居中
     *
     * @return
     */
    public static WriteHandler styleCenter() {
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle,
                contentWriteCellStyle);
        return horizontalCellStyleStrategy;
    }

    /**
     * excel单个sheet导出
     *
     * @param fileName 文件名，带后缀，eg:excel.xlsx
     * @param sheetName sheetName
     * @param exportType 导出自定义模板
     * @param list 导出集合
     * @param writeHandler 样式
     */
    public static <T> void export(String fileName, String sheetName, Class<T> exportType, List<T> list,
                                  WriteHandler writeHandler) {
        //创建文件
        String filePath = getFilePath(fileName);

        //判断样式
        if (Objects.nonNull(writeHandler)) {
            EasyExcel.write(filePath, exportType).registerWriteHandler(writeHandler).sheet(sheetName).doWrite(list);
        } else {
            EasyExcel.write(filePath, exportType).sheet(sheetName).doWrite(list);
        }
    }

    /**
     * excel导出多个sheet
     *
     * @param fileName 文件名，带后缀，eg:excel.xlsx
     * @param exportList sheet集合
     * @param writeHandler 全局样式
     */
    public static void export(String fileName, List<ExcelExportObject> exportList, WriteHandler writeHandler) {
        //创建文件
        String filePath = getFilePath(fileName);
        //指定文件
        ExcelWriter excelWriter;
        //指定全局样式
        if (Objects.nonNull(writeHandler)) {
            excelWriter = EasyExcel.write(filePath).registerWriteHandler(writeHandler).build();
        } else {
            excelWriter = EasyExcel.write(filePath).build();
        }
        //指定sheet页
        int i = 0;
        for (ExcelExportObject excelExportObject : exportList) {
            //判断Sheet样式
            WriteSheet writeSheet;
            if (Objects.nonNull(excelExportObject.getWriteHandler())) {
                writeSheet = EasyExcel.writerSheet(i, excelExportObject.getSheetName())
                        .head(excelExportObject.getExportType())
                        .registerWriteHandler(excelExportObject.getWriteHandler()).build();
            } else {
                writeSheet = EasyExcel.writerSheet(i, excelExportObject.getSheetName())
                        .head(excelExportObject.getExportType()).build();
            }
            //写入Sheet
            excelWriter.write(excelExportObject.getDataList(), writeSheet);
            i++;
        }
        //关闭流
        excelWriter.finish();
    }

    private static String getFilePath(String fileName) {
        //获取路径
        String excelUploadPath = "";
        String path = excelUploadPath.trim();
        //创建文件夹
        File fileOld = new File(path);
        if (!fileOld.exists()) {
            try {
                fileOld.mkdirs();
            } catch (Exception e) {
                log.error(path + ",创建文件夹失败", e);
            }
        }
        //写入文件绝对地址
        return String.format("%s%s", path, fileName);
    }

    /**
     * excel单个sheet导出
     *
     * @param list 导出集合
     */
    public static <T> void export(List<T> list, ExcelWriter excelWriter, WriteSheet writeSheet) {
        //创建文件
        excelWriter.write(list, writeSheet);
    }

    public static <T> WriteSheet getWriteSheet(String sheetName, Class<T> exportType, WriteHandler writeHandler) {
        WriteSheet writeSheet;
        if (Objects.nonNull(writeHandler)) {
            writeSheet = EasyExcel.writerSheet(null, sheetName).head(exportType).registerWriteHandler(writeHandler)
                    .build();
        } else {
            writeSheet = EasyExcel.writerSheet(null, sheetName).head(exportType).build();
        }
        return writeSheet;
    }

    public static <T> ExcelWriter getWriter(String fileName, Class<T> exportType, WriteHandler writeHandler) {
        ExcelWriter excelWriter;
        //创建文件
        String filePath = getFilePath(fileName);
        //判断样式
        if (Objects.nonNull(writeHandler)) {
            excelWriter = EasyExcel.write(filePath).registerWriteHandler(writeHandler).build();
        } else {
            excelWriter = EasyExcel.write(filePath).build();
        }
        return excelWriter;
    }

}
