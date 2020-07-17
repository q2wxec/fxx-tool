package com.fxx.common.tools.db.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.exception.ToolException;
import com.fxx.common.tools.utils.BeanUtils;
import com.fxx.common.tools.utils.CollUtils;

/**
 * @author wangxiao1
 * @date 2020/7/14
 */
public class JoinUtils {
    /**
     * @param mainData 主数据
     * @param resultType 关联结果数据类型
     * @param callBack 单行组装结果回调
     * @param joinBeans 从数据封装
     * @param <T> 关联结果数据类型
     * @return
     */
    public static <T> List<T> dataJoin(List<Object> mainData, Class<T> resultType, JoinCallBack<T> callBack,
                                       JoinBean... joinBeans) {
        List<T> joinResult = new ArrayList<T>();
        if (joinBeans == null || joinBeans.length == 0) {
            return joinResult;
        }
        if (CollUtils.isNotEmpty(mainData)) {
            mainData.stream().forEach(o -> {
                T t = null;
                try {
                    t = resultType.newInstance();
                } catch (Exception e) {
                    throw new ToolException("数据关联实例化返回值异常！", e);
                }
                BeanUtils.copyProperties(o, t);
                for (JoinBean joinBean : joinBeans) {
                    String mainColumn = joinBean.getMainColumn();
                    Map<Object, Object> dataMap = joinBean.getDataMap();
                    Boolean strictJoin = joinBean.getStrictJoin();
                    Object fieldValue = BeanUtils.getFieldValue(o, mainColumn);
                    ToolAssert.notNull(fieldValue, "数据关联字段不允许为空值！");
                    Object subData = dataMap.get(fieldValue);
                    if (strictJoin) {
                        ToolAssert.notNull(subData, "严格关联，从数据不可为空！");
                    }
                    BeanUtils.copyProperties(subData, t);
                }
                if (callBack != null) {
                    callBack.joinCallBack(t);
                }
                joinResult.add(t);
            });
        }
        return joinResult;
    }
}
