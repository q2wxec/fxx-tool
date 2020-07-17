package com.fxx.common.tools.exception;

import com.fxx.common.tools.utils.ArrayUtils;
import com.fxx.common.tools.utils.CollUtils;
import com.fxx.common.tools.utils.StrUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author wangxiao1
 * @date 2020/6/12
 */
public class ToolAssert {
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }


    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ToolException(message);
        }
    }


    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new ToolException(message);
        }
    }


    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ToolException(message);
        }
    }


    public static void hasLength(String text, String message) {
        if (!StrUtils.isNotEmpty(text)) {
            throw new ToolException(message);
        }
    }


    public static void hasText(String text, String message) {
        if (StrUtils.isBlank(text)) {
            throw new ToolException(message);
        }
    }


    public static void doesNotContain(String textToSearch, String substring, String message) {
        if (StrUtils.isNotEmpty(textToSearch) && StrUtils.isNotEmpty(substring) && textToSearch.contains(substring)) {
            throw new ToolException(message);
        }
    }


    public static void notEmpty(Object[] array, String message) {
        if (ArrayUtils.isEmpty(array)) {
            throw new ToolException(message);
        }
    }


    public static void noNullElements(Object[] array, String message) {
        if (array != null) {
            Object[] var2 = array;
            int var3 = array.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object element = var2[var4];
                if (element == null) {
                    throw new ToolException(message);
                }
            }
        }

    }


    public static void notEmpty(Collection<?> collection, String message) {
        if (CollUtils.isEmpty(collection)) {
            throw new ToolException(message);
        }
    }


    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollUtils.isEmpty(map)) {
            throw new ToolException(message);
        }
    }


    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, message);
        }

    }

    public static void isInstanceOf(Class<?> type, Object obj) {
        isInstanceOf(type, obj, "");
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, message);
        }

    }

    public static void isAssignable(Class<?> superType, Class<?> subType) {
        isAssignable(superType, subType, "");
    }

    private static void instanceCheckFailed(Class<?> type, Object obj, String msg) {
        String className = obj != null ? obj.getClass().getName() : "null";
        String result = "";
        boolean defaultMessage = true;
        if (StrUtils.isNotEmpty(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, className);
                defaultMessage = false;
            }
        }

        if (defaultMessage) {
            result = result + "Object of class [" + className + "] must be an instance of " + type;
        }

        throw new ToolException(result);
    }

    private static void assignableCheckFailed(Class<?> superType, Class<?> subType, String msg) {
        String result = "";
        boolean defaultMessage = true;
        if (StrUtils.isNotEmpty(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, subType);
                defaultMessage = false;
            }
        }

        if (defaultMessage) {
            result = result + subType + " is not assignable to " + superType;
        }

        throw new ToolException(result);
    }

    private static boolean endsWithSeparator(String msg) {
        return msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith(".");
    }

    private static String messageWithTypeName(String msg, Object typeName) {
        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }

}
