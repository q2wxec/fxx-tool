
package com.fxx.common.tools.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.exception.ToolException;
import com.fxx.common.tools.status.CompensateTypeEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2019/10/818:01 sc远程调用封装工具类，封装出入参打印及失败信息入库处理
 */
@Slf4j
public class MethodRetryUtils {

    /**
     * @param clientClass 远程调用接口class，请通过接口.class方式传入，使用对象getClass将导致问题
     * @param methodName 方法名
     * @param requestParams 请求参数数组， 1.为空的时候需传入一个空的Object数组，
     *            2.需确保参数列表中不存在null值，不然无法判断参数类型，无法进行反射方法调用
     * @param scCompensateTypeEnum 是否需要重发补偿
     * @return
     */
    public static Object remoteExcute(Object client, Class clientClass, String methodName, List<String> requestParams,
                                      CompensateTypeEnum scCompensateTypeEnum) {

        log.info("{}方法{}查询列表的入参{}", clientClass.getName(), methodName, JsonUtils.toJSONString(requestParams));
        //Object client = SpringContextHolder.getApplicationContext().getBean(clientClass);
        ToolAssert.notNull(client, clientClass.getName() + "远程调用接口在spring上下文中找不到的实现类");
        Method declaredMethod = null;
        if (requestParams == null) {
            requestParams = CollUtils.newArrayList();
        }
        declaredMethod = getMethodByNameAndParamsNum(client.getClass(), methodName, requestParams.size());
        ToolAssert.notNull(declaredMethod, "找不到" + clientClass.getName() + "接口," + "调用的远程方法" + methodName);

        Object result = null;
        Object[] requestParamsWithType = castJsonArrayToParamsWithType(requestParams, declaredMethod);
        try {
            result = declaredMethod.invoke(client, requestParamsWithType);
        } catch (InvocationTargetException e) {
            throw new ToolException(clientClass.getName() + "接口," + "调用的远程方法：" + methodName + "异常", e);
        } catch (Exception e) {
            //e.printStackTrace();
            log.error(clientClass.getName() + "接口,方法" + methodName + "反射调用失败，请检查具体调用参数", e);
            throw new ToolException(clientClass.getName() + "接口,方法" + methodName + "反射调用失败，请检查具体调用参数", e);
        }
        log.info("{}方法{}查询列表的出参{}", clientClass.getName(), methodName, JsonUtils.toJSONString(result));
        return result;
    }

    public static Method getMethodByNameAndParamsNum(Class claz, String methodName, int paramsNum) {
        Method[] declaredMethods = claz.getDeclaredMethods();
        Method m = null;
        int i = 0;
        for (Method method : declaredMethods) {
            String name = method.getName();
            int parameterCount = method.getParameterCount();
            if (name.equals(methodName) && parameterCount == paramsNum) {
                m = method;
                i++;
            }
        }
        ToolAssert.isTrue(i == 1, "class:" + claz + "中的方法：" + methodName + "存在同名且参数个数相同的情况，无法处理！！");
        return m;
    }

    /**
     * 将JSON按方法的参数类型还原
     *
     * @param requestParams
     * @param declaredMethod
     * @return
     */
    public static Object[] castJsonArrayToParamsWithType(List<String> requestParams, Method declaredMethod) {
        Object[] requestParamsWithType = new Object[requestParams.size()];
        Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
        for (int i = 0; i < requestParams.size(); i++) {
            Object requestParam = requestParams.get(i);
            Object paramWithType = null;
            Class<?> parameterType = parameterTypes[i];
            if (requestParam == null) {
                paramWithType = null;
            } else {
                paramWithType = JsonUtils.toJavaObject(requestParam, parameterType);
            }
            requestParamsWithType[i] = paramWithType;
        }
        return requestParamsWithType;
    }

    public static void main(String[] args) {
        Object[] objects = new Object[] { 1, 1L, 1.1D, true, new Date() };
        String s = JsonUtils.toJSONString(objects);
        List<String> strings = JsonUtils.toJavaObjectList(s, String.class);
        MethodRetryUtils remoteExcuteUtils = new MethodRetryUtils();
        Object testMethod = remoteExcute(remoteExcuteUtils, MethodRetryUtils.class, "testMethod", strings,
                CompensateTypeEnum.NEED);
        System.out.println(testMethod);
    }

    public int testMethod(int a, long b, double c, boolean d, Date e) {
        return 5;
    }

}
