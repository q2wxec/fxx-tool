package com.fxx.common.tools.db;


import com.fxx.common.tools.utils.CollUtils;
import com.fxx.common.tools.utils.ConvertUtils;
import com.fxx.common.tools.utils.StrUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * @author wangxiao1
 * @date 2019/12/1911:43
 */
public class DbNullConstant {
    //1900-01-01 00:00:00
    public static final Date DATE_NULL = new Date(-2209017600000L);

    public static final String STRING_NULL = "";

    public static final Long ID_NULL = 0L;

    public static final BigDecimal BIGDECIMAL_NULL = new BigDecimal(-1);

    public static final Integer NUMBER_NULL = -1;

    public static Boolean isNullDate(Date date) {
        if (date == null || DATE_NULL.equals(date)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    public static Boolean isNullId(Long id) {
        if (id == null || ID_NULL.equals(id)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    public static Boolean isNullNumber(Number number) {
        if (number == null) {
            return Boolean.TRUE;
        }
        Number cast = ConvertUtils.convert(number.getClass(), NUMBER_NULL);
        if (number instanceof BigDecimal) {
            BigDecimal num = (BigDecimal) number;
            BigDecimal castNum = (BigDecimal) cast;
            return 0 == castNum.compareTo(num);
        } else {
            return number.equals(cast);
        }
    }


    public static Collection setListNullToEmpty(Collection collection) {
        return setListNullToEmpty(collection, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
    }

    public static Collection setListNullToEmpty(Collection collection, Boolean dealDate, Boolean dealString, Boolean dealNumber) {
        if (CollUtils.isEmpty(collection)) {
            return collection;
        }
        Collection newCollection = null;
        try {
            newCollection = collection.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("集合无法通过反射空参创建！", e);
        }
        Collection finalNewCollection = newCollection;
        collection.stream().forEach(obj -> {
            Object o = setNullToEmpty(obj, dealDate, dealString, dealNumber);
            finalNewCollection.add(o);
        });
        return finalNewCollection;
    }

    public static Object setNullToEmpty(Object object) {
        return setNullToEmpty(object, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
    }

    public static Object setNullToEmpty(Object object, Boolean dealDate, Boolean dealString, Boolean dealNumber) {
        if (object == null) {
            return object;
        }
        if (object instanceof Collection) {
            return setListNullToEmpty((Collection) object, dealDate, dealString, dealNumber);
        }
        Class objClass = object.getClass();
        Field[] fields = objClass.getDeclaredFields();
        try {
            for (Field field : fields) {
                //设置属性是可以访问的
                field.setAccessible(true);
                //1、获取属性上的指定类型的注解
                Object o = field.get(object);
                Class<?> type = field.getType();
                if (o == null) {
                    if (Number.class.isAssignableFrom(type) && dealNumber) {
                        Object cast = ConvertUtils.convert(type, NUMBER_NULL);
                        field.set(object, cast);
                    } else if (String.class.isAssignableFrom(type) && dealString) {
                        field.set(object, STRING_NULL);
                    } else if (Date.class.isAssignableFrom(type) && dealDate) {
                        field.set(object, DATE_NULL);
                    }
                } else if (type.isAssignableFrom(Collection.class)) {
                    Collection collection = setListNullToEmpty((Collection) o, dealDate, dealString, dealNumber);
                    field.set(object, collection);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("反射异常！", e);
        }
        return object;
    }


    public static Collection setListEmptyToNull(Collection collection, Boolean dealDate, Boolean dealString, Boolean dealNumber) {
        if (CollUtils.isEmpty(collection)) {
            return collection;
        }
        Collection newCollection = null;
        try {
            newCollection = collection.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("集合无法通过反射空参创建！", e);
        }
        Collection finalNewCollection = newCollection;
        collection.stream().forEach(obj -> {
            Object o = setEmptyToNull(obj, dealDate, dealString, dealNumber);
            finalNewCollection.add(o);
        });
        return finalNewCollection;
    }


    public static Object setEmptyToNull(Object object, Boolean dealDate, Boolean dealString, Boolean dealNumber) {
        if (object == null) {
            return object;
        }
        if (object instanceof Collection) {
            return setListEmptyToNull((Collection) object, dealDate, dealString, dealNumber);
        }
        Class objClass = object.getClass();
        Field[] fields = objClass.getDeclaredFields();
        try {
            for (Field field : fields) {
                //设置属性是可以访问的
                field.setAccessible(true);
                //1、获取属性上的指定类型的注解
                Object o = field.get(object);
                Class<?> type = field.getType();
                if (o != null) {
                    if (Number.class.isAssignableFrom(type) && dealNumber && isNullNumber((Number) o)) {
                        field.set(object, null);
                    } else if (String.class.isAssignableFrom(type) && dealString && StrUtils.isBlank(String.valueOf(o))) {
                        field.set(object, null);
                    } else if (Date.class.isAssignableFrom(type) && dealDate && isNullDate((Date) o)) {
                        field.set(object, null);
                    }
                } else if (type.isAssignableFrom(Collection.class)) {
                    Collection collection = setListEmptyToNull((Collection) o, dealDate, dealString, dealNumber);
                    field.set(object, collection);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("反射异常！", e);
        }
        return object;
    }

}
