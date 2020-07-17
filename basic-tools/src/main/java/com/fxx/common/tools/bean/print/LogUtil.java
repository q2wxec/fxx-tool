
package com.fxx.common.tools.bean.print;

import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.utils.CollUtils;
import com.fxx.common.tools.utils.StrUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author wangxiao1
 * @date 2019/9/2513:58
 */
@Slf4j
public class LogUtil {
    //换行符
    public static final String LINE_CHANGE_TAG = "&&&";
    //list行号标识
    public static final String LIST_ORDER_TAG = "#";
    //默认精准型格式化方式
    public static final String BIGDECIMAL_FORMATE = "0.00";
    //空行
    public static final String LINE_BREAK_TAG = " ";
    //删除展示示例
    public static final String LIST_DELETE_MSG = "删除";


    /**
     * @param object    属性字段
     * @param list      列表展示部分
     * @param listTitle 列表展示主标题
     * @return
     * @throws Exception
     */
    public static String insertLog(Object object, List list, String listTitle) throws Exception {
        return insertLog(object, list, listTitle, null, null);
    }

    /**
     * @param object      属性字段
     * @param list        列表展示部分
     * @param listTitle   列表展示主标题
     * @param ignorFields 因部分场景需要展示属性标注了LogLable注解，但按需求在另一些场景不展示
     * @return
     * @throws Exception
     */
    public static String insertLog(Object object, List list, String listTitle, Set<String> ignorFields, Set<String> listIgnorFields) throws Exception {
        String objLog = insertLogBean(object, ignorFields);
        String listLog = insertLogList(list, listIgnorFields);
        if (StrUtils.isBlank(listLog)) {
            return objLog;
        }
        return objLog + LINE_CHANGE_TAG + LINE_BREAK_TAG + LINE_CHANGE_TAG + listTitle + LINE_CHANGE_TAG + listLog;
    }

    /**
     * 需要转换的属性需要标注javax.xml.bind.annotation.XmlElement注解
     * 若该属性类型未定义toString()方法，则输出值为Object.toString()结果
     * 因而用于日志处理的属性切记需要实现toString方法,用于在日志中展示的value
     *
     * @param object
     * @return
     * @throws Exception
     */
    public static String insertLogBean(Object object) throws Exception {
        return insertLogBean(object, null);
    }

    /**
     * 需要转换的属性需要标注javax.xml.bind.annotation.XmlElement注解
     * 若该属性类型未定义toString()方法，则输出值为Object.toString()结果
     * 因而用于日志处理的属性切记需要实现toString方法,用于在日志中展示的value
     *
     * @param object
     * @param ignorFields 因部分场景需要展示属性标注了LogLable注解，但按需求在另一些场景不展示
     * @return
     * @throws Exception
     */
    public static String insertLogBean(Object object, Set<String> ignorFields) throws Exception {
        Class objClass = object.getClass();
        Field[] fields = objClass.getDeclaredFields();
        StringBuffer sb = new StringBuffer();

        for (Field field : fields) {

            //设置属性是可以访问的
            field.setAccessible(true);
            //1、获取属性上的指定类型的注解
            Annotation annotation = field.getAnnotation(LogLable.class);
            //有该类型的注解存在
            if (annotation != null) {
                //得到此属性的值
                Object value = field.get(object);
                String name = field.getName();
                String type = field.getType().getName();
                //针对多个需求使用
                if (ignorFields != null && ignorFields.contains(name)) {
                    continue;
                }
                //强制转化为相应的注解
                LogLable logLable = (LogLable) annotation;
                String label = logLable.name();

                value = dealWithValue(field, value);
                if (StrUtils.isNotBlank(String.valueOf(value))) {
                    sb.append(LINE_CHANGE_TAG).append(label).append("：").append(String.valueOf(value));
                }
            }
        }
        sb = removeStartLineChangeTag(sb);
        return sb.toString();
    }


    /**
     * @param list
     * @return
     * @throws Exception
     */
    public static String insertLogList(List list, Set<String> listIgnorFields) throws Exception {
        if (null == list || list.size() == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            sb.append(LINE_CHANGE_TAG).append(LIST_ORDER_TAG).append(i + 1).append(LINE_CHANGE_TAG)
                    .append(insertLogBean(obj, listIgnorFields));
        }
        sb = removeStartLineChangeTag(sb);
        return sb.toString();
    }

    /**
     * @param list
     * @return
     * @throws Exception
     */
    public static String insertLogList(List list) throws Exception {
        return insertLogList(list, null);
    }

    public static UpdateLogResult updateLogDoNotIgnorNull(Object objOld, Object objNew, List listOld, List listNew, String listTitle) throws Exception {
        return updateLog(objOld, objNew, listOld, listNew, listTitle, false);
    }

    public static UpdateLogResult updateLog(Object objOld, Object objNew, List listOld, List listNew, String listTitle, boolean ignorIfAfterNull) throws Exception {
        UpdateLogResult updateLogBeanResult = updateLogBeanCompare(objOld, objNew, ignorIfAfterNull);
        UpdateLogResult updateLogListResult = updateLogListCompare(listOld, listNew, ignorIfAfterNull);

        String before = updateLogBeanResult.getBefore();
        String beforeList = updateLogListResult.getBefore();
        if (!StrUtils.isBlank(beforeList)) {
            before = before + LINE_CHANGE_TAG + LINE_BREAK_TAG + LINE_CHANGE_TAG + listTitle + LINE_CHANGE_TAG + beforeList;
        }
        String after = updateLogBeanResult.getAfter();
        String afterList = updateLogListResult.getAfter();
        if (!StrUtils.isBlank(afterList)) {
            after = after + LINE_CHANGE_TAG + LINE_BREAK_TAG + LINE_CHANGE_TAG + listTitle + LINE_CHANGE_TAG + afterList;
        }
        UpdateLogResult result = new UpdateLogResult();
        result.setBefore(before);
        result.setAfter(after);
        return result;
    }


    /**
     * 需要转换的属性需要标注javax.xml.bind.annotation.XmlElement注解
     * 若该属性类型未定义toString()方法，则输出值为Object.toString()结果
     * 因而用于日志处理的属性切记需要实现toString方法,用于在日志中展示的value
     *
     * @param objOld
     * @param objNew 默认比较所有字段，包括修改前为null的字段
     * @return
     * @throws Exception
     */
    public static UpdateLogResult updateLogBeanCompareDoNotIgnorNull(Object objOld, Object objNew) throws Exception {
        return updateLogBeanCompare(objOld, objNew, false);
    }


    /**
     * 需要转换的属性需要标注javax.xml.bind.annotation.XmlElement注解
     * 若该属性类型未定义toString()方法，则输出值为Object.toString()结果
     * 因而用于日志处理的属性切记需要实现toString方法,用于在日志中展示的value
     *
     * @param objOld
     * @param objNew
     * @param ignorIfAfterNull 为true时当修改前值为null的时候不记录改变
     * @return
     * @throws Exception
     */
    public static UpdateLogResult updateLogBeanCompare(Object objOld, Object objNew, boolean ignorIfAfterNull) throws Exception {
        StringBuffer beforeSb = new StringBuffer();
        StringBuffer afterSb = new StringBuffer();

        Class clasOld = objOld.getClass();
        Class clasNew = objNew.getClass();
        ToolAssert.isTrue(clasOld.isInstance(objNew), "LogUtil.updateLogBeanCompare:传入的两个java对象类型不一致！");
        Field[] fields = clasOld.getDeclaredFields();
        for (Field field : fields) {

            Annotation annotation = field.getAnnotation(LogLable.class);
            //有该类型的注解存在
            if (annotation != null) {

                //强制转化为相应的注解
                field.setAccessible(true);
                LogLable logLable = (LogLable) annotation;
                String label = logLable.name();
                String name = field.getName();
                String type = field.getType().getName();
                Object valOld = field.get(objOld);
                Object valNew = field.get(objNew);
                if (ignorIfAfterNull && null == valNew) {
                    continue;
                }
                valOld = dealWithValue(field, valOld);
                valNew = dealWithValue(field, valNew);

                boolean b = logLable.alwaysLogWhenEdit();
                if (b) {
                    beforeSb.append(LINE_CHANGE_TAG).append(label).append("：").append(String.valueOf(valOld));
                    afterSb.append(LINE_CHANGE_TAG).append(label).append("：").append(String.valueOf(valNew));
                } else if (!String.valueOf(valOld).equals(String.valueOf(valNew))) {
                    beforeSb.append(LINE_CHANGE_TAG).append(label).append("：").append(String.valueOf(valOld));
                    afterSb.append(LINE_CHANGE_TAG).append(label).append("：").append(String.valueOf(valNew));
                }
            }
        }

        beforeSb = removeStartLineChangeTag(beforeSb);
        afterSb = removeStartLineChangeTag(afterSb);
        UpdateLogResult updateLogResult = new UpdateLogResult();
        updateLogResult.setBefore(beforeSb.toString());
        updateLogResult.setAfter(afterSb.toString());
        return updateLogResult;
    }


    /**
     * 用于对比的list，需要存储一致的对象类型
     *
     * @param listOld
     * @param listNew
     * @return
     * @throws Exception
     */
    public static UpdateLogResult updateLogListCompareDoNotIgnorNull(List listOld, List listNew) throws Exception {
        return updateLogListCompare(listOld, listNew, false);
    }

    /**
     * 用于对比的list，需要存储一致的对象类型
     *
     * @param listOld
     * @param listNew
     * @return
     * @throws Exception
     */
    public static UpdateLogResult updateLogListCompare(List listOld, List listNew, boolean ignorIfAfterNull) throws Exception {
        StringBuffer beforeSb = new StringBuffer();
        StringBuffer afterSb = new StringBuffer();

        if ((null == listOld || listOld.size() == 0) && (null == listNew || listNew.size() == 0)) {
            log.warn("LogUtil.updateLogListCompare传入的两个list均为空，无法比较");
            return new UpdateLogResult("", "");
        }
        Object obj = null;
        if (null == listOld || listOld.size() == 0) {
            obj = listNew.get(0);
            listOld = CollUtils.newArrayList();
        } else {
            obj = listOld.get(0);
        }
        Class claz = obj.getClass();
        LogIdForListCompare logIdForListCompare = obj.getClass().getAnnotation(LogIdForListCompare.class);
        if (logIdForListCompare == null) {
            log.warn("LogUtil.updateLogListCompare待比较的对象未注解LogIdForListCompare，无法比较");
            return new UpdateLogResult("", "");
        }
        String idField = logIdForListCompare.idField();
        LinkedHashMap afterMap = new LinkedHashMap();
        for (Object object : listNew) {
            Field declaredField = claz.getDeclaredField(idField);
            declaredField.setAccessible(true);
            Object idVal = declaredField.get(object);
            ToolAssert.notNull(idVal, "LogUtil.updateLogListCompare,list列表遍历时出现id为空的情况");
            afterMap.put(idVal, object);
        }

        for (int i = 0; i < listOld.size(); i++) {
            Object beforeObj = listOld.get(i);
            Field declaredField = claz.getDeclaredField(idField);
            declaredField.setAccessible(true);
            Object idVal = declaredField.get(beforeObj);
            ToolAssert.notNull(idVal, "LogUtil.updateLogListCompare,list列表遍历时出现id为空的情况");
            if (!afterMap.containsKey(idVal)) {
                beforeSb.append(LINE_CHANGE_TAG).append(LIST_ORDER_TAG).append(i + 1).append(LINE_CHANGE_TAG)
                        .append(insertLogBean(beforeObj));
                afterSb.append(LINE_CHANGE_TAG).append(LIST_ORDER_TAG).append(i + 1).append(LINE_CHANGE_TAG)
                        .append(LIST_DELETE_MSG);
            } else {
                Object afterObj = afterMap.get(idVal);
                UpdateLogResult updateLogResult = updateLogBeanCompare(beforeObj, afterObj, ignorIfAfterNull);
                if (updateLogResult.getBefore().equals(updateLogResult.getAfter())) {
                    afterMap.remove(idVal);
                    continue;
                }
                beforeSb.append(LINE_CHANGE_TAG).append(LIST_ORDER_TAG).append(i + 1).append(LINE_CHANGE_TAG)
                        .append(updateLogResult.getBefore());
                afterSb.append(LINE_CHANGE_TAG).append(LIST_ORDER_TAG).append(i + 1).append(LINE_CHANGE_TAG)
                        .append(updateLogResult.getAfter());
                afterMap.remove(idVal);
            }
        }
        int flag = 1;
        //Set set = afterMap.keySet();
        Set<Map.Entry> entrySet = afterMap.entrySet();
        for (Map.Entry entry : entrySet) {
            afterSb.append(LINE_CHANGE_TAG).append(LIST_ORDER_TAG).append(listOld.size() + flag).append(LINE_CHANGE_TAG)
                    .append(insertLogBean(entry.getValue()));
            flag++;
        }

        beforeSb = removeStartLineChangeTag(beforeSb);
        afterSb = removeStartLineChangeTag(afterSb);
        UpdateLogResult updateLogResult = new UpdateLogResult();
        updateLogResult.setBefore(beforeSb.toString());
        updateLogResult.setAfter(afterSb.toString());
        return updateLogResult;
    }

    private static String dealWithValue(Field field, Object value) throws IllegalAccessException, InstantiationException {
        String type = field.getType().getName();
        LogFormat logFormat = field.getAnnotation(LogFormat.class);
        if (logFormat != null) {
            Class<? extends LogFieldFormatInter> logFieldFormatInterClass = logFormat.formatInter();
            LogFieldFormatInter logFieldFormatInter = null;
            if (logFieldFormatInterClass != null) {
                if (logFieldFormatInterClass.isEnum()) {
                    logFieldFormatInter = logFieldFormatInterClass.getEnumConstants()[0];
                } else {
                    logFieldFormatInter = logFieldFormatInterClass.newInstance();
                }
                String format = logFieldFormatInter.format(value);
                return format != null ? format : "";
            }
        }

        LogTypeFormat logTypeFormat = field.getAnnotation(LogTypeFormat.class);
        if (logTypeFormat != null) {
            String[] strings = logTypeFormat.logTypes();
            for (String t : strings) {
                String[] split = t.split(",");
                int code = Integer.parseInt(split[0]);
                if (String.valueOf(value).equals(String.valueOf(code))) {
                    return split[1];
                }
            }
            return "";
        }

        if (null == value) {
            return dealWithNull(value);
        }
        if ("java.util.Date".equals(type)) {
            return dealWithDate(field, value);
        }
        if ("java.math.BigDecimal".equals(type)) {
            return dealWithBigDecimal(field, value);
        }
        return String.valueOf(value);
    }

    private static String dealWithDate(Field field, Object value) {
        String type = field.getType().getName();
        ToolAssert.isTrue("java.util.Date".equals(type), "LogUtil.dealWithDate值不为日期类型，处理方法错误");
        if (value == null) {
            return null;
        }
        Annotation formateAnnotation = field.getAnnotation(LogDateFormat.class);
        if (formateAnnotation != null) {
            LogDateFormat logDateFormat = (LogDateFormat) formateAnnotation;
            String format = logDateFormat.format();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            if (null != value) {
                Date valDate = (Date) value;
                value = sdf.format(valDate);
            }
        }
        return String.valueOf(value);
    }

    private static String dealWithBigDecimal(Field field, Object value) {
        String type = field.getType().getName();
        ToolAssert.isTrue("java.math.BigDecimal".equals(type), "LogUtil.dealWithBigDecimal值不为精准类型，处理方法错误");
        if (value == null) {
            return null;
        }
        String decimalFormat = BIGDECIMAL_FORMATE;
        Annotation formateAnnotation = field.getAnnotation(LogBigDecimalFormat.class);
        if (formateAnnotation != null) {
            LogBigDecimalFormat logBigDecimalFormat = (LogBigDecimalFormat) formateAnnotation;
            decimalFormat = logBigDecimalFormat.format();
        }
        BigDecimal valBig = new BigDecimal(String.valueOf(value));
        DecimalFormat formatter1 = new DecimalFormat(decimalFormat);
        String format = formatter1.format(valBig);
        return format;
    }

    private static String dealWithNull(Object value) {
        ToolAssert.isTrue(value == null, "LogUtil.dealWithNull值不为空，处理方法错误");
        return "";
    }

    private static StringBuffer removeStartLineChangeTag(StringBuffer sb) {
        if (sb.length() > 0) {
            sb.delete(0, LINE_CHANGE_TAG.length());
        }
        return sb;
    }

}
