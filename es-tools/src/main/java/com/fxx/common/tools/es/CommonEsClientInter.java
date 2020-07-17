
package com.fxx.common.tools.es;

import com.fxx.common.tools.es.modal.EsBaseProperties;
import com.fxx.common.tools.es.modal.PageInfo;
import com.fxx.common.tools.es.modal.ScrollInfo;
import com.fxx.common.tools.es.wrapper.AggregationEsWrapper;
import com.fxx.common.tools.es.wrapper.BaseEsWrapper;
import com.fxx.common.tools.es.wrapper.EsEntityWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.SearchHits;

import java.util.List;
import java.util.Map;

/**
 * @author wangxiao1
 * @date 2020/6/12
 */
public interface CommonEsClientInter {
    <A> A getById(String id, Class<A> aClass);

    Map<String, Object> getById(EsBaseProperties esBaseProperties);

    <A> PageInfo<A> queryEntityPage(EsEntityWrapper<A> esEntityWrapper, Integer page, Integer size);

    <A> ScrollInfo<A> queryEntityScroll(EsEntityWrapper<A> esEntityWrapper, Long scrollAliveMinutes, String scrollId, Integer size);

    <A> List<A> queryList(EsEntityWrapper<A> esEntityWrapper);

    PageInfo<Map<String, Object>> queryForPage(String[] indexs, String[] types, Integer page, Integer size, BaseEsWrapper baseEsWrapper);

    ScrollInfo<Map<String, Object>> queryForScrool(String[] indexs, String[] types, Long scrollAliveMinutes, String scrollId, Integer size, BaseEsWrapper baseEsWrapper);

    List<Map<String, Object>> aggregationQuery(EsEntityWrapper esEntityWrapper);

    List<Map<String, Object>> aggregationQuery(String[] indexs, String[] types, AggregationEsWrapper aggregationEsWrapper);

    List<Map<String, Object>> query(String[] indexs, String[] types, BaseEsWrapper baseEsWrapper);

    SearchHits query(SearchRequest searchQuery);

    String add(Object entity);

    String addWithId(Object entity);

    String add(String jsonStr, EsBaseProperties esBaseProperties);

    String addWithId(String jsonStr, EsBaseProperties esBaseProperties);

    Integer bulkAdd(List entyties);

    Integer bulkAddWithId(List entyties);

    Integer bulkAddOrUpdateWithId(List entyties);

    Boolean deleteById(String id, Class aClass);

    Boolean deleteById(EsBaseProperties esBaseProperties);

    Boolean updateById(Object entity);

    Boolean updateById(String jsonStr, EsBaseProperties esBaseProperties);

    List<Map<String, Object>> getResultList(SearchHits hits);
}
