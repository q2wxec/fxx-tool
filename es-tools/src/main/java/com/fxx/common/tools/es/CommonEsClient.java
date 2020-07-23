
package com.fxx.common.tools.es;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fxx.common.tools.es.anotation.BaseEsModal;
import com.fxx.common.tools.es.anotation.EsModalId;
import com.fxx.common.tools.es.exception.EsException;
import com.fxx.common.tools.es.modal.EsBaseProperties;
import com.fxx.common.tools.es.modal.PageInfo;
import com.fxx.common.tools.es.modal.ScrollInfo;
import com.fxx.common.tools.es.wrapper.AggregationEsWrapper;
import com.fxx.common.tools.es.wrapper.AggregationTypeEnum;
import com.fxx.common.tools.es.wrapper.BaseEsWrapper;
import com.fxx.common.tools.es.wrapper.EsEntityWrapper;
import com.fxx.common.tools.exception.ToolAssert;
import com.fxx.common.tools.utils.BeanUtils;
import com.fxx.common.tools.utils.CollUtils;
import com.fxx.common.tools.utils.JsonUtils;
import com.fxx.common.tools.utils.StrUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxiao1
 * @date 2019/12/1119:26
 */
@Slf4j
public class CommonEsClient implements CommonEsClientInter {

    public CommonEsClient(RestHighLevelClient client) {
        this.client = client;
    }

    public CommonEsClient(RestHighLevelClient client, Boolean esQueryLog) {
        this.client = client;
        this.esQueryLog = esQueryLog;
    }

    public CommonEsClient(RestHighLevelClient client, Boolean esQueryLog, String indexPrfix) {
        this.client = client;
        this.esQueryLog = esQueryLog;
        this.indexPrfix = indexPrfix;
    }

    private RestHighLevelClient client;

    private Boolean esQueryLog = Boolean.TRUE;

    private String indexPrfix = "";

    public static final int ES_INDEX_MAX_RESULT_WINDOW = 60000;
    public static final int ES_DEFAULT_PAGE = 1;
    public static final int ES_DEFAULT_SIZE = 10;
    public static final long ES_DEFAULT_SCROLLALIVE = 1L;
    public static final String ES_TIMESTAMP_KEY = "@timestamp";
    public static final String ES_INSERT_TIME = "es_insert_time";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static final String ID = "id";

    /**
     * @param id     存入ES的主键
     * @param aClass 存入ES的对象class，需为注解为{@link BaseEsModal},且在主键上标注{@link EsModalId}的class
     * @param <A>
     * @return
     */
    @Override
    public <A> A getById(String id, Class<A> aClass) {
        EsBaseProperties esBaseProperties = getEsBaseProperties(aClass);
        esBaseProperties.setId(id);
        Map<String, Object> source = getById(esBaseProperties);
        if (source == null) {
            log.info("ES查询，查询配置:{}，查询id:{},结果集为空！", esBaseProperties, id);
            return null;
        }
        A a = BeanUtils.mapToBean(source, aClass, Boolean.TRUE);
        Object o = source.get(ID);
        setEsModalId(a, o);
        return a;
    }

    /**
     * @param esBaseProperties 需传入，id，index，type{@link EsBaseProperties}
     * @return 查询数据的Map
     */
    @Override
    public Map<String, Object> getById(EsBaseProperties esBaseProperties) {
        basePropertiesValid(esBaseProperties);
        GetRequest getRequest = new GetRequest(esBaseProperties.getIndex(), esBaseProperties.getType(), esBaseProperties.getId());
        GetResponse result = null;
        boolean indexExists = isIndexExists(esBaseProperties.getIndex());
        if (!indexExists) {
            return CollUtils.newHashMap();
        }
        try {
            result = client.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-getById发生IO异常，无法获取数据，入参{}", esBaseProperties, e);
            throw new EsException("ES-getById发生IO异常，无法获取数据，入参" + esBaseProperties, e);
        }
        Map<String, Object> source = result.getSource();
        if (source != null) {
            source.put(ID, result.getId());
        }
        return source;
    }

    /**
     * @param page            查询页数
     * @param size            分页size
     * @param esEntityWrapper 查询条件Wrapper{@link EsEntityWrapper}
     * @return
     */
    @Override
    public <A> PageInfo<A> queryEntityPage(EsEntityWrapper<A> esEntityWrapper, Integer page, Integer size) {
        Class<A> entityClass = esEntityWrapper.getEntityClass();
        EsBaseProperties esBaseProperties = getEsBaseProperties(entityClass);
        String[] indexs = new String[]{esBaseProperties.getIndex()};
        String[] types = new String[]{esBaseProperties.getType()};
        PageInfo<Map<String, Object>> query = this.queryForPage(indexs, types, page, size, esEntityWrapper);
        List<Map<String, Object>> queryList = query.getList();
        PageInfo<A> objectPageInfo = new PageInfo<>();
        if (CollUtils.isEmpty(queryList)) {
            return objectPageInfo;
        }
        BeanUtils.copyProperties(query, objectPageInfo);
        List<A> entities = getEntityList(queryList, entityClass);
        objectPageInfo.setList(entities);
        return objectPageInfo;
    }

    /**
     * @param scrollAliveMinutes 查询页数
     * @param scrollId           查询页数
     * @param size               分页size
     * @param esEntityWrapper    查询条件Wrapper{@link EsEntityWrapper}
     * @return
     */
    @Override
    public <A> ScrollInfo<A> queryEntityScroll(EsEntityWrapper<A> esEntityWrapper, Long scrollAliveMinutes, String scrollId, Integer size) {
        Class<A> entityClass = esEntityWrapper.getEntityClass();
        EsBaseProperties esBaseProperties = getEsBaseProperties(entityClass);
        String[] indexs = new String[]{esBaseProperties.getIndex()};
        String[] types = new String[]{esBaseProperties.getType()};
        ScrollInfo<Map<String, Object>> mapScrollInfo = this.queryForScrool(indexs, types, scrollAliveMinutes, scrollId, size, esEntityWrapper);
        List<Map<String, Object>> queryList = mapScrollInfo.getList();
        ScrollInfo<A> objectPageInfo = new ScrollInfo<>();
        BeanUtils.copyProperties(mapScrollInfo, objectPageInfo);
        if (CollUtils.isEmpty(queryList)) {
            return objectPageInfo;
        }
        List<A> entities = getEntityList(queryList, entityClass);
        objectPageInfo.setList(entities);
        return objectPageInfo;
    }

    /**
     * @param esEntityWrapper 查询条件Wrapper{@link EsEntityWrapper}
     *                        ES index.max_result_window 限制一次最多查询 10000条数据
     * @return
     */
    @Override
    public <A> List<A> queryList(EsEntityWrapper<A> esEntityWrapper) {
        Class<A> entityClass = esEntityWrapper.getEntityClass();
        EsBaseProperties esBaseProperties = getEsBaseProperties(entityClass);
        String[] indexs = new String[]{esBaseProperties.getIndex()};
        String[] types = new String[]{esBaseProperties.getType()};
        List<Map<String, Object>> query = this.query(indexs, types, esEntityWrapper);
        List<A> entities = getEntityList(query, entityClass);
        return entities;
    }

    /**
     * @param indexs        查询的所有索引
     * @param types         查询的所有类型
     * @param page          查询页数
     * @param size          分页size
     * @param baseEsWrapper 查询条件Wrapper
     * @return
     */
    @Override
    public PageInfo<Map<String, Object>> queryForPage(String[] indexs, String[] types, Integer page, Integer size, BaseEsWrapper baseEsWrapper) {
        if (page == null) {
            page = ES_DEFAULT_PAGE;
            ;
        }
        if (size == null) {
            size = ES_DEFAULT_SIZE;
        }
        return query(indexs, types, page, size, baseEsWrapper.getTermParams(), baseEsWrapper.getLikeParams(), baseEsWrapper.getLteParams(), baseEsWrapper.getGteParams(), baseEsWrapper.getLtParams(), baseEsWrapper.getGtParams(), baseEsWrapper.getInParams(), baseEsWrapper.getSortParams());
    }

    /**
     * @param indexs        查询的所有索引
     * @param types         查询的所有类型
     * @param scrollId      滚动游标
     * @param size          分页size
     * @param baseEsWrapper 查询条件Wrapper
     * @return
     */
    @Override
    public ScrollInfo<Map<String, Object>> queryForScrool(String[] indexs, String[] types, Long scrollAliveMinutes, String scrollId, Integer size, BaseEsWrapper baseEsWrapper) {
        return scrollQuery(indexs, types, scrollAliveMinutes, scrollId, size, baseEsWrapper.getTermParams(), baseEsWrapper.getLikeParams(), baseEsWrapper.getLteParams(), baseEsWrapper.getGteParams(), baseEsWrapper.getLtParams(), baseEsWrapper.getGtParams(), baseEsWrapper.getInParams(), baseEsWrapper.getSortParams());
    }


    /**
     * @return
     */
    @Override
    public List<Map<String, Object>> aggregationQuery(EsEntityWrapper esEntityWrapper) {
        Class entityClass = esEntityWrapper.getEntityClass();
        EsBaseProperties esBaseProperties = getEsBaseProperties(entityClass);
        String[] indexs = new String[]{esBaseProperties.getIndex()};
        String[] types = new String[]{esBaseProperties.getType()};
        return aggregationQuery(indexs, types, esEntityWrapper);
    }

    /**
     * @param indexs               查询的所有索引
     * @param types                查询的所有类型
     * @param aggregationEsWrapper 查询条件Wrapper
     * @return
     */
    @Override
    public List<Map<String, Object>> aggregationQuery(String[] indexs, String[] types, AggregationEsWrapper aggregationEsWrapper) {
        // 在prepareSearch()的参数为索引库列表，意为要从哪些索引库中进行查询
        if (!checkIndex(indexs)) {
            log.warn("aggregationQuery,索引{}不存在，返回空！", indexs);
            return CollUtils.newArrayList();
        }
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(aggregationEsWrapper);
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(null, boolQueryBuilder, null);
        List<AggregationBuilder> aggregationBuilders = getAggregationBuilder(aggregationEsWrapper);
        ToolAssert.notEmpty(aggregationBuilders, "聚合查询参数为空无法进行聚合查询");
        for (AggregationBuilder aggregationBuilder : aggregationBuilders) {
            searchSourceBuilder.aggregation(aggregationBuilder);
        }
        SearchRequest searchRequest = getSearchRequest(indexs, types, searchSourceBuilder);
        //3、发送请求
        SearchResponse response = null;
        try {
            response = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-query发生IO异常，无法查询数据，入参{}", searchRequest, e);
            throw new EsException("ES-query发生IO异常，无法查询数据，入参" + searchRequest, e);
        }
        ToolAssert.isTrue(RestStatus.OK.equals(response.status()), "ES查询返回异常！status：" + response.status());
        List<Map<String, Object>> aggResult = getAggResult(response);
        return aggResult;
    }


    /**
     * @param indexs        查询的所有索引
     * @param types         查询的所有类型
     * @param baseEsWrapper 查询条件Wrapper
     *                      ES index.max_result_window 限制一次最多查询 10000条数据
     * @return
     */
    @Override
    public List<Map<String, Object>> query(String[] indexs, String[] types, BaseEsWrapper baseEsWrapper) {
        PageInfo<Map<String, Object>> query = query(indexs, types, ES_DEFAULT_PAGE, ES_INDEX_MAX_RESULT_WINDOW, baseEsWrapper.getTermParams(), baseEsWrapper.getLikeParams(), baseEsWrapper.getLteParams(), baseEsWrapper.getGteParams(), baseEsWrapper.getLtParams(), baseEsWrapper.getGtParams(), baseEsWrapper.getInParams(), baseEsWrapper.getSortParams());
        long total = query.getTotal();
        //JavaAssert.isTrue(total>ES_INDEX_MAX_RESULT_WINDOW,"当前查询条件下查询数据超过"+ES_INDEX_MAX_RESULT_WINDOW+"条，超过ES窗口限制，请使用分页或滚动查询方式！");
        return query.getList();
    }


    /**
     * @param searchQuery 自定义ES，SearchRequestBuilder
     *                    ES index.max_result_window 限制一次最多查询 10000条数据
     * @return
     */
    @Override
    public SearchHits query(SearchRequest searchQuery) {
        SearchResponse response = null;
        try {
            response = client.search(searchQuery, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-query发生IO异常，无法查询数据，入参{}", searchQuery, e);
            throw new EsException("ES-query发生IO异常，无法查询数据，入参" + searchQuery, e);
        }
        ToolAssert.isTrue(RestStatus.OK.equals(response.status()), "ES查询返回异常！status：" + response.status());
        SearchHits hits = response.getHits();
        return hits;
    }

    /**
     * 添加文档
     *
     * @param entity 需为注解为{@link BaseEsModal},且在主键上标注{@link EsModalId}的对象
     * @return id
     * id自动生成，若需指定id，请使用addWithId,生成id默认为String类型，实体中标注{@link EsModalId}的属性需要为String
     */
    @Override
    public String add(Object entity) {
        EsBaseProperties esBaseProperties = getEsBaseProperties(entity.getClass());
        String str = JsonUtils.toJSONString(entity);
        return add(str, esBaseProperties);
    }

    /**
     * 添加文档
     *
     * @param entity 需为注解为{@link BaseEsModal},且在主键上标注{@link EsModalId}的对象
     * @return id
     */
    @Override
    public String addWithId(Object entity) {
        EsBaseProperties esBaseProperties = getEsBasePropertiesWithId(entity);
        String str = JsonUtils.toJSONString(entity);
        return addWithId(str, esBaseProperties);
    }


    /**
     * @param jsonStr          存入ES的JSON字符串
     * @param esBaseProperties 需传入，index，type
     *                         id自动生成，若需指定id，请使用addWithId
     * @return
     */
    @Override
    public String add(String jsonStr, EsBaseProperties esBaseProperties) {
        basePropertiesValidWithoutId(esBaseProperties);
        ToolAssert.isTrue(JsonUtils.isValidJSON(jsonStr), "传入数据非JSON格式，无法进行索引处理！");
        String jsonStrWithTime = addTimeFile(jsonStr);
        IndexRequest indexRequest = new IndexRequest(esBaseProperties.getIndex(), esBaseProperties.getType());
        indexRequest.source(jsonStrWithTime, XContentType.JSON);
        IndexResponse result = null;
        try {
            result = client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-add发生IO异常，无法插入数据，入参jsonStr：{}，入参esBaseProperties：{}", jsonStr, esBaseProperties, e);
            throw new EsException("ES-add发生IO异常，无法插入数据，入参jsonStr：" + jsonStr + "，入参esBaseProperties：" + esBaseProperties, e);
        }
        return result.getId();
    }

    /**
     * @param jsonStr          存入ES的JSON字符串
     * @param esBaseProperties 需传入，id，index，type
     * @return
     */
    @Override
    public String addWithId(String jsonStr, EsBaseProperties esBaseProperties) {
        basePropertiesValid(esBaseProperties);
        ToolAssert.isTrue(JsonUtils.isValidJSON(jsonStr), "传入数据非JSON格式，无法进行索引处理！");
        String jsonStrWithTime = addTimeFile(jsonStr);
        IndexRequest indexRequest = new IndexRequest(esBaseProperties.getIndex(), esBaseProperties.getType(), esBaseProperties.getId());
        indexRequest.source(jsonStrWithTime, XContentType.JSON);
        IndexResponse result = null;
        try {
            result = client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-addWithId发生IO异常，无法插入数据，入参jsonStr：{}，入参esBaseProperties：{}", jsonStr, esBaseProperties, e);
            throw new EsException("ES-addWithId发生IO异常，无法插入数据，入参jsonStr：" + jsonStr + "，入参esBaseProperties：" + esBaseProperties, e);
        }
        return result.getId();
    }

    /**
     * 批量新增
     *
     * @param entyties 实体List
     *                 id自动生成，若需指定id，请使用bulkAddWithId
     * @return
     */
    @Override
    public Integer bulkAdd(List entyties) {
        if (CollUtils.isEmpty(entyties)) {
            return 0;
        }
        List requests = CollUtils.newArrayList();
        log.info("预计批量插入条数：{}", entyties.size());
        for (Object entity : entyties) {
            EsBaseProperties esBaseProperties = getEsBaseProperties(entity.getClass());
            IndexRequest indexRequest = new IndexRequest(esBaseProperties.getIndex(), esBaseProperties.getType());
            String str = JsonUtils.toJSONString(entity);
            String jsonStrWithTime = addTimeFile(str);
            indexRequest.opType(DocWriteRequest.OpType.INDEX);
            indexRequest.source(jsonStrWithTime, XContentType.JSON);
            requests.add(indexRequest);
        }
        Integer count = bulkOperate(requests);
        log.info("成功插入ES条数：{}", count);
        return count;
    }


    /**
     * 批量新增
     *
     * @param entyties 实体List
     *                 id指定
     * @return
     */
    @Override
    public Integer bulkAddWithId(List entyties) {
        if (CollUtils.isEmpty(entyties)) {
            return 0;
        }
        List requests = CollUtils.newArrayList();
        log.info("预计批量插入条数：{}", entyties.size());
        for (Object entity : entyties) {
            EsBaseProperties esBaseProperties = getEsBasePropertiesWithId(entity);
            IndexRequest indexRequest = new IndexRequest(esBaseProperties.getIndex(), esBaseProperties.getType(), esBaseProperties.getId());
            String str = JsonUtils.toJSONString(entity);
            String jsonStrWithTime = addTimeFile(str);
            indexRequest.opType(DocWriteRequest.OpType.CREATE);
            indexRequest.source(jsonStrWithTime, XContentType.JSON);
            requests.add(indexRequest);
        }
        Integer count = bulkOperate(requests);
        log.info("成功插入ES条数：{}", count);
        return count;
    }


    /**
     * 批量新增
     *
     * @param entyties 实体List
     *                 id指定
     * @return
     */
    @Override
    public Integer bulkAddOrUpdateWithId(List entyties) {
        if (CollUtils.isEmpty(entyties)) {
            return 0;
        }
        List requests = CollUtils.newArrayList();
        log.info("预计批量插入或更新条数：{}", entyties.size());
        for (Object entity : entyties) {
            EsBaseProperties esBaseProperties = getEsBasePropertiesWithId(entity);
            IndexRequest indexRequest = new IndexRequest(esBaseProperties.getIndex(), esBaseProperties.getType(), esBaseProperties.getId());
            String str = JsonUtils.toJSONString(entity);
            String jsonStrWithTime = addTimeFile(str);
            indexRequest.opType(DocWriteRequest.OpType.INDEX);
            indexRequest.source(jsonStrWithTime, XContentType.JSON);
            requests.add(indexRequest);
        }
        Integer count = bulkOperate(requests);
        log.info("成功插入或更新ES条数：{}", count);
        return count;
    }

    /**
     * 根据id删除
     *
     * @param id     存入ES的主键
     * @param aClass 待删除ES的对象class
     */
    @Override
    public Boolean deleteById(String id, Class aClass) {
        EsBaseProperties esBaseProperties = getEsBaseProperties(aClass);
        esBaseProperties.setId(id);
        return deleteById(esBaseProperties);
    }

    /**
     * @param esBaseProperties 需传入，id，index，type
     * @return
     */
    @Override
    public Boolean deleteById(EsBaseProperties esBaseProperties) {
        DeleteRequest deleteRequest = new DeleteRequest(esBaseProperties.getIndex(), esBaseProperties.getType(), esBaseProperties.getId());
        DeleteResponse result = null;
        try {
            result = client.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-deleteById发生IO异常，无法删除数据，入参esBaseProperties：{}", esBaseProperties, e);
            throw new EsException("ES-deleteById发生IO异常，无法删除数据，入参esBaseProperties：{}" + esBaseProperties, e);
        }
        return DocWriteResponse.Result.DELETED.equals(result.getResult());
    }


    /**
     * @param entity 需为注解为{@link BaseEsModal},且在主键上标注{@link EsModalId}的对象
     * @return
     */
    @Override
    public Boolean updateById(Object entity) {
        EsBaseProperties esBaseProperties = getEsBasePropertiesWithId(entity);
        String str = JsonUtils.toJSONString(entity);
        return updateById(str, esBaseProperties);
    }

    /**
     * @param jsonStr          待更新ES的JSON字符串
     * @param esBaseProperties 需传入，id，index，type
     * @return
     */
    @Override
    public Boolean updateById(String jsonStr, EsBaseProperties esBaseProperties) {
        basePropertiesValid(esBaseProperties);
        ToolAssert.isTrue(JsonUtils.isValidJSON(jsonStr), "传入数据非JSON格式，无法进行索引处理！");
        log.info("ES更新，更新配置{}，更新Json{}", esBaseProperties, jsonStr);
        // 根据id查询
        UpdateRequest updateRequest = new UpdateRequest(esBaseProperties.getIndex(), esBaseProperties.getType(), esBaseProperties.getId());
        updateRequest.doc(jsonStr, XContentType.JSON);
        // 进行更新
        UpdateResponse updateResponse = null;
        try {
            updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-updateById发生IO异常，无法更新数据，入参jsonStr：{}，入参esBaseProperties：{}", jsonStr, esBaseProperties, e);
            throw new EsException("ES-updateById发生IO异常，无法更新数据，入参jsonStr：" + jsonStr + "，入参esBaseProperties：" + esBaseProperties, e);
        }
        return DocWriteResponse.Result.UPDATED.equals(updateResponse.getResult());
    }

    private EsBaseProperties getEsBaseProperties(Class entityClass) {
        ToolAssert.notNull(entityClass, "传入class为null，无法进行Es相关操作");
        BaseEsModal baseEsModal = (BaseEsModal) entityClass.getAnnotation(BaseEsModal.class);
        //有该类型的注解存在
        ToolAssert.notNull(baseEsModal, "class:{" + entityClass.getName() + "},非BaseEsModal注解对象，无法使用通用EsClient");
        EsBaseProperties esBaseProperties = new EsBaseProperties();
        esBaseProperties.setIndex(baseEsModal.index());
        esBaseProperties.setCreateIndex(baseEsModal.createIndex());
        esBaseProperties.setIndexStoreType(baseEsModal.indexStoreType());
        esBaseProperties.setRefreshInterval(baseEsModal.refreshInterval());
        esBaseProperties.setReplicas(baseEsModal.replicas());
        esBaseProperties.setShards(baseEsModal.shards());
        esBaseProperties.setIndexPrfix(indexPrfix);
        return esBaseProperties;
    }

    /**
     * @param indexs     查询的所有索引
     * @param types      查询的所有类型
     * @param page       查询页数
     * @param size       分页size
     * @param termParams 相等查询参数
     * @param likeParams 模糊查询参数 直接传入模糊查询的字段名及模糊查询的值即可，不需添加 %或*的通配符
     * @param lteParams  小于等于查询参数
     * @param gteParams  大于等于查询参数
     * @param sortParams 用于排序的字段参数，优先级从前到后{@link SortOrder}
     * @return
     */
    private PageInfo<Map<String, Object>> query(String[] indexs, String[] types, Integer page, Integer size, Map<String, Object> termParams, Map<String, String> likeParams, Map<String, Object> lteParams, Map<String, Object> gteParams, Map<String, Object> ltParams, Map<String, Object> gtParams, Map<String, List> inParams, LinkedHashMap<String, SortOrder> sortParams) {
        // 在prepareSearch()的参数为索引库列表，意为要从哪些索引库中进行查询
        if (!checkIndex(indexs)) {
            log.warn("aggregationQuery,索引{}不存在，返回空！", indexs);
            return new PageInfo<>();
        }
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(termParams, likeParams, lteParams, gteParams, ltParams, gtParams, inParams);
        Integer windowSize = null;
        if (page != null && size != null && page > 0 && size > 0) {
            windowSize = page * size;
        }
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(sortParams, boolQueryBuilder, windowSize);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>();
        if (page != null && size != null && page > 0 && size > 0) {
            Integer from = (page - 1) * size;
            searchSourceBuilder.from(from).size(size);
            pageInfo.setPageNum(page);
            pageInfo.setPageSize(size);
        }
        SearchRequest searchRequest = getSearchRequest(indexs, types, searchSourceBuilder);
        if (esQueryLog) {
            log.info(searchRequest.toString());
        }
        SearchHits hits = query(searchRequest);
        List<Map<String, Object>> resultList = getResultList(hits);
        pageInfo.setList(resultList);
        pageInfo.setTotal(hits.totalHits);
        return pageInfo;
    }


    /**
     * @param indexs      查询的所有索引
     * @param types       查询的所有类型
     * @param scrollAlive scroll存活时间,分钟
     * @param size        分页size
     * @param termParams  相等查询参数
     * @param likeParams  模糊查询参数 直接传入模糊查询的字段名及模糊查询的值即可，不需添加 %或*的通配符
     * @param lteParams   小于等于查询参数
     * @param gteParams   大于等于查询参数
     * @param sortParams  用于排序的字段参数，优先级从前到后{@link SortOrder}
     * @return
     */
    private ScrollInfo<Map<String, Object>> scrollQuery(String[] indexs, String[] types, Long scrollAlive, String scrollId, Integer size, Map<String, Object> termParams, Map<String, String> likeParams, Map<String, Object> lteParams, Map<String, Object> gteParams, Map<String, Object> ltParams, Map<String, Object> gtParams, Map<String, List> inParams, LinkedHashMap<String, SortOrder> sortParams) {
        // 在prepareSearch()的参数为索引库列表，意为要从哪些索引库中进行查询
        if (!checkIndex(indexs)) {
            log.warn("aggregationQuery,索引{}不存在，返回空！", indexs);
            return new ScrollInfo<>();
        }
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(termParams, likeParams, lteParams, gteParams, ltParams, gtParams, inParams);
        SearchSourceBuilder searchSourceBuilder = getSearchSourceBuilder(sortParams, boolQueryBuilder, null);
        if (size == null) {
            size = ES_DEFAULT_SIZE;
        }
        searchSourceBuilder.size(size);
        SearchRequest searchRequest = getSearchRequest(indexs, types, searchSourceBuilder);
        if (scrollAlive == null) {
            scrollAlive = ES_DEFAULT_SCROLLALIVE;
        }
        searchRequest.scroll(TimeValue.timeValueMinutes(scrollAlive));
        ScrollInfo scrollInfo = new ScrollInfo();
        scrollInfo.setPageSize(size);
        try {
            if (StrUtils.isBlank(scrollId)) {
                log.info("first scroll");
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                String scrollIdReturn = searchResponse.getScrollId();
                SearchHits hits = searchResponse.getHits();
                List<Map<String, Object>> resultList = getResultList(hits);
                scrollInfo.setTotal(hits.totalHits);
                scrollInfo.setList(resultList);
                scrollInfo.setScrollId(scrollIdReturn);
            } else {
                log.info("loop  scroll");
                Scroll scroll = new Scroll(TimeValue.timeValueMinutes(scrollAlive));
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                SearchResponse searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                String scrollIdReturn = searchResponse.getScrollId();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    log.info("scroll完成");
                    clearScroll(scrollId);
                    scrollInfo.setScrollEnd(Boolean.TRUE);
                    return scrollInfo;
                }
                List<Map<String, Object>> resultList = getResultList(hits);
                scrollInfo.setTotal(hits.totalHits);
                scrollInfo.setList(resultList);
                scrollInfo.setScrollId(scrollIdReturn);
            }
        } catch (IOException e) {
            log.error("ES-scrollQuery发生IO异常，无法查询数据，入参{}", searchRequest, e);
            throw new EsException("ES-scrollQuery发生IO异常，无法查询数据，入参" + searchRequest, e);
        }
        return scrollInfo;
    }


    @Override
    public List<Map<String, Object>> getResultList(SearchHits hits) {
        List<Map<String, Object>> resultList = CollUtils.newArrayList();
        if (hits == null) {
            return resultList;
        }
        for (SearchHit hit : hits) {
            //将获取的值转换成map的形式
            Map<String, Object> map = hit.getSourceAsMap();
            if (map != null) {
                map.put(ID, hit.getId());
            }
            resultList.add(map);
        }
        return resultList;
    }


    private SearchRequest getSearchRequest(String[] indexs, String[] types, SearchSourceBuilder searchSourceBuilder) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(indexs);
        if (types != null && types.length > 0) {
            searchRequest.types(types);
        }
        // 设置查询类型，有QUERY_AND_FETCH  QUERY_THEN_FETCH  DFS_QUERY_AND_FETCH  DFS_QUERY_THEN_FETCH
        searchRequest.searchType(SearchType.DEFAULT);
        return searchRequest;
    }

    private SearchSourceBuilder getSearchSourceBuilder(LinkedHashMap<String, SortOrder> sortParams, QueryBuilder queryBuilder, Integer size) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (sortParams != null) {
            for (Map.Entry<String, SortOrder> entry : sortParams.entrySet()) {
                String key = entry.getKey();
                SortOrder value = entry.getValue();
                if (StrUtils.isNotBlank(key) && value != null && StrUtils.isNotBlank(String.valueOf(value))) {
                    searchSourceBuilder.sort(key, value);
                }
            }
        }
        searchSourceBuilder.query(queryBuilder);
        if (size == null) {
            size = ES_INDEX_MAX_RESULT_WINDOW;
        }
        searchSourceBuilder.size(size);
        return searchSourceBuilder;
    }

    private void clearScroll(String scrollId) throws IOException {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        if (clearScrollResponse.isSucceeded()) {
            log.info("ES滚动查询，id:{},清除成功！", scrollId);
        } else {
            log.warn("ES滚动查询，id:{},清除失败！", scrollId);
        }
    }

    private BoolQueryBuilder getBoolQueryBuilder(BaseEsWrapper baseEsWrapper) {
        return getBoolQueryBuilder(baseEsWrapper.getTermParams(), baseEsWrapper.getLikeParams(), baseEsWrapper.getLteParams(), baseEsWrapper.getGteParams(), baseEsWrapper.getLtParams(), baseEsWrapper.getGtParams(), baseEsWrapper.getInParams());
    }


    private BoolQueryBuilder getBoolQueryBuilder(Map<String, Object> termParams, Map<String, String> likeParams, Map<String, Object> lteParams, Map<String, Object> gteParams, Map<String, Object> ltParams, Map<String, Object> gtParams, Map<String, List> inParams) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (termParams != null) {
            for (Map.Entry<String, Object> entry : termParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (StrUtils.isNotBlank(key) && value != null && StrUtils.isNotBlank(String.valueOf(value))) {
                    boolQueryBuilder.must(QueryBuilders.termQuery(key, value));
                }
            }
        }
        //wildcardQuery 使用keyword，可以避免查询词被es分词
        if (likeParams != null) {
            for (Map.Entry<String, String> entry : likeParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (StrUtils.isNotBlank(key) && StrUtils.isNotBlank(value)) {
                    boolQueryBuilder.must(QueryBuilders.wildcardQuery(key, value));
                }
            }
        }
        if (lteParams != null) {
            for (Map.Entry<String, Object> entry : lteParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (StrUtils.isNotBlank(key) && value != null && StrUtils.isNotBlank(String.valueOf(value))) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery(key).lte(value));
                }
            }
        }

        if (gteParams != null) {
            for (Map.Entry<String, Object> entry : gteParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (StrUtils.isNotBlank(key) && value != null && StrUtils.isNotBlank(String.valueOf(value))) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(value));
                }
            }
        }

        if (ltParams != null) {
            for (Map.Entry<String, Object> entry : ltParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (StrUtils.isNotBlank(key) && value != null && StrUtils.isNotBlank(String.valueOf(value))) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery(key).lt(value));
                }
            }
        }

        if (gtParams != null) {
            for (Map.Entry<String, Object> entry : gtParams.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (StrUtils.isNotBlank(key) && value != null && StrUtils.isNotBlank(String.valueOf(value))) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery(key).gt(value));
                }
            }
        }
        if (inParams != null) {
            for (Map.Entry<String, List> entry : inParams.entrySet()) {
                String key = entry.getKey();
                List value = entry.getValue();
                if (StrUtils.isNotBlank(key) && CollUtils.isNotEmpty(value)) {
                    BoolQueryBuilder inBoolQueryBuilder = QueryBuilders.boolQuery();
                    for (Object obj : value) {
                        if (obj != null && StrUtils.isNotBlank(String.valueOf(obj))) {
                            inBoolQueryBuilder.should(QueryBuilders.termsQuery(key, obj));
                        }
                    }
                    inBoolQueryBuilder.minimumShouldMatch(1);
                    boolQueryBuilder.must(inBoolQueryBuilder);
                }
            }
        }
        return boolQueryBuilder;
    }

    private EsBaseProperties getEsBasePropertiesWithId(Object entity) {
        ToolAssert.notNull(entity, "传入对象为null，无法进行Es相关操作");
        Class<?> entityClass = entity.getClass();
        EsBaseProperties esBaseProperties = getEsBaseProperties(entityClass);
        String id = getEsModalId(entity);
        ToolAssert.hasText(id, "class:{" + entityClass.getName() + "},找不到EsModalId标注的属性，或者EsModalId标注的属性为空");
        esBaseProperties.setId(id);
        return esBaseProperties;
    }

    private String getEsModalId(Object entity) {
        Class<?> entityClass = entity.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            //设置属性是可以访问的
            field.setAccessible(true);
            //1、获取属性上的指定类型的注解
            Annotation annotation = field.getAnnotation(EsModalId.class);
            //有该类型的注解存在
            if (annotation != null) {
                //得到此属性的值
                try {
                    Object o = field.get(entity);
                    return o == null ? null : String.valueOf(o);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 带id添加情况，将携带id，设置为ES，_id
     *
     * @param entity
     * @param id
     */
    private void setEsModalId(Object entity, Object id) {
        String esModalId = getEsModalId(entity);
        if (esModalId != null) {
            return;
        }
        Class<?> entityClass = entity.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            //设置属性是可以访问的
            field.setAccessible(true);
            //1、获取属性上的指定类型的注解
            Annotation annotation = field.getAnnotation(EsModalId.class);
            //有该类型的注解存在
            if (annotation != null) {
                //得到此属性的值
                if (id != null) {
                    try {
                        field.set(entity, id);
                    } catch (Exception e) {
                        log.warn("ES查询设置ID异常！");
                    }
                }
                break;
            }
        }
    }

    private void basePropertiesValid(EsBaseProperties esBaseProperties) {
        ToolAssert.notNull(esBaseProperties, "ES基础配置为NULL无法操作！");
        ToolAssert.hasText(esBaseProperties.getId(), "ES配置ID为空无法操作！");
        ToolAssert.hasText(esBaseProperties.getIndex(), "ES配置INDEX为空无法操作！");
        ToolAssert.hasText(esBaseProperties.getType(), "ES配置Type为空无法操作！");
    }

    private void basePropertiesValidWithoutId(EsBaseProperties esBaseProperties) {
        ToolAssert.notNull(esBaseProperties, "ES基础配置为NULL无法操作！");
        ToolAssert.hasText(esBaseProperties.getIndex(), "ES配置INDEX为空无法操作！");
        ToolAssert.hasText(esBaseProperties.getType(), "ES配置Type为空无法操作！");
    }

    /**
     * 批处理操作封装
     *
     * @param requests
     * @return
     */
    private Integer bulkOperate(List<DocWriteRequest> requests) {
        if (CollUtils.isEmpty(requests)) {
            return 0;
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (DocWriteRequest request : requests) {
            bulkRequest.add(request);
        }
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-bulkOperate发生IO异常，无法批量修改数据，入参{}", requests, e);
            throw new EsException("ES-bulkOperate发生IO异常，无法批量修改数据，入参" + requests, e);
        }
        //4、处理响应
        if (bulkResponse != null) {
            BulkItemResponse[] items = bulkResponse.getItems();
            Long count = Arrays.stream(items).filter(bulkItemResponse -> !bulkItemResponse.isFailed()).count();
            return count.intValue();
        }
        return 0;
    }

    private <A> List<A> getEntityList(List<Map<String, Object>> query, Class<A> entityClass) {
        List<A> entities = CollUtils.newArrayList();
        if (CollUtils.isEmpty(query)) {
            return entities;
        }
        for (Map<String, Object> map : query) {
            Object o = map.get(ID);
            A a = BeanUtils.mapToBean(map, entityClass, Boolean.TRUE);
            setEsModalId(a, o);
            entities.add(a);
        }
        return entities;
    }

    private boolean checkIndex(String[] indexs) {
        ToolAssert.notNull(indexs, "查询索引不可为空！");
        ToolAssert.isTrue(indexs.length != 0, "查询索引不可为空！");
        return isIndexExists(indexs);
    }

    /**
     * 获取聚合查询request
     *
     * @param aggregationEsWrapper
     * @return
     */
    private List<AggregationBuilder> getAggregationBuilder(AggregationEsWrapper aggregationEsWrapper) {
        List<AggregationBuilder> aggregationBuilders = new ArrayList<>();
        //加入聚合
        Set<String> groupFileds = aggregationEsWrapper.getGroupFileds();
        //ES聚合为同套桶模式，第一个桶为最终返回查询的入口，最后的桶链接聚合查询的内容如，max，min
        //因而需要记录第一个和最后一个桶
        TermsAggregationBuilder first = null;
        TermsAggregationBuilder last = null;
        //桶套桶，并记录第一个和最后一个桶
        for (String groupFiled : groupFileds) {
            TermsAggregationBuilder temp = AggregationBuilders.terms(groupFiled).field(groupFiled);
            if (first != null) {
                first.subAggregation(temp);
            } else {
                first = temp;
            }
            last = temp;
        }
        LinkedHashMap<String, Boolean> aggregationSortParams = aggregationEsWrapper.getAggregationSortParams();
        //若无桶存在，则说明是对所有文档聚合，不存在排序要求
        //若有桶存在则将排序调价加到各分组桶
        if (last != null) {
            for (Map.Entry<String, Boolean> entry : aggregationSortParams.entrySet()) {
                String orderField = entry.getKey();
                Boolean isAsc = entry.getValue();
                last.order(BucketOrder.aggregation(orderField, isAsc));
            }
        }
        Map<String, AggregationTypeEnum> aggregationFileds = aggregationEsWrapper.getAggregationFileds();
        for (Map.Entry<String, AggregationTypeEnum> aggregationFiled : aggregationFileds.entrySet()) {
            String aggregationColumn = aggregationFiled.getKey();
            AggregationTypeEnum typeEnum = aggregationFiled.getValue();
            AggregationBuilder temp = null;
            switch (typeEnum) {
                case AVG:
                    temp = AggregationBuilders.avg(AggregationEsWrapper.getAvgField(aggregationColumn)).field(aggregationColumn);
                    break;
                case MAX:
                    temp = AggregationBuilders.max(AggregationEsWrapper.getMaxField(aggregationColumn)).field(aggregationColumn);
                    break;
                case MIN:
                    temp = AggregationBuilders.min(AggregationEsWrapper.getMinField(aggregationColumn)).field(aggregationColumn);
                    break;
                case SUM:
                    temp = AggregationBuilders.sum(AggregationEsWrapper.getSumField(aggregationColumn)).field(aggregationColumn);
                    break;
                default:
                    break;
            }
            //将聚合查询要求附加到最后一个桶，或直接组成list
            if (temp != null && last != null) {
                last.subAggregation(temp);
            } else if (temp != null) {
                aggregationBuilders.add(temp);
            }
        }
        //默认添加count聚合
        AggregationBuilder temp = AggregationBuilders.count(AggregationEsWrapper.COUNT).field("_id");
        //直接返回第一个桶，或返回聚合查询list（无桶情况）
        if (first != null) {
            last.subAggregation(temp);
            aggregationBuilders.add(first);
        } else {
            aggregationBuilders.add(temp);
        }
        return aggregationBuilders;
    }

    private List<Map<String, Object>> getAggResult(SearchResponse response) {
        // 获取聚合结果
        List<Map<String, Object>> res = new ArrayList<>();
        Aggregations aggregations = response.getAggregations();
        HashMap map = new HashMap();
        recurseMap(res, aggregations, map);
        return res;
    }

    /**
     * 递归处理聚合结果，结束逻辑：当前的agg下没有stringterm聚合
     *
     * @param resList      结果集合
     * @param aggregations 聚合结果，根据业务，每层只有一个stringterm聚合
     * @param prototype    上层传来的原型，需要将上层的原型复制到本层，并将本层的map副本作为下层的原型
     * @return
     */
    private void recurseMap(List<Map<String, Object>> resList, Aggregations aggregations,
                            Map<String, Object> prototype) {
        //递归找到最底层的桶，获取最底层桶的聚合结果
        //当子节点桶不为空，且桶挂载节点不为空进入递归
        for (Aggregation aggregation : aggregations) {
            if (aggregation instanceof Terms) {
                Terms terms = null;
                terms = (Terms) aggregation;
                // 复制原型
                Map<String, Object> thisTurnProptype = new HashMap<>(prototype);
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                // 没有数据就结束执行
                if (buckets.isEmpty()) {
                    continue;
                }

                // 增加原型数据，继续进入递归
                String aggKey = terms.getName();
                for (Terms.Bucket thisTrunBucket : buckets) {
                    String aggValue = thisTrunBucket.getKeyAsString();
                    thisTurnProptype.put(aggKey, aggValue);
                    if (thisTrunBucket.getAggregations() == null || thisTrunBucket.getAggregations().asList().isEmpty()) {
                        continue;
                    }
                    recurseMap(resList, thisTrunBucket.getAggregations(), thisTurnProptype);
                }
            }
        }
        //当本层存在聚合结果直接聚合本层并添加到结果集
        if (!allAggIsTerms(aggregations)) {
            // 复制原型
            Map<String, Object> thisTurnProptype = new HashMap<>(prototype);
            Map<String, Object> aggregationsMap = getAggregationsMap(aggregations);
            thisTurnProptype.putAll(aggregationsMap);
            resList.add(thisTurnProptype);
        }
    }

    //判断本节点下是否全是桶类型
    private Boolean allAggIsTerms(Aggregations aggs) {
        if (aggs == null || aggs.asList().size() == 0) {
            return Boolean.TRUE;
        }
        for (Aggregation agg : aggs) {
            if (!(agg instanceof Terms)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    //获取本层聚合结果
    private Map<String, Object> getAggregationsMap(Aggregations aggs) {
        Map<String, Object> item = new HashMap<>();
        for (Aggregation agg : aggs) {
            String aggName = agg.getName();
            Object value = null;
            if (agg instanceof Max) {
                Max max = (Max) agg;
                value = max.getValue();
            } else if (agg instanceof Min) {
                Min min = (Min) agg;
                value = min.getValue();
            } else if (agg instanceof Avg) {
                Avg avg = (Avg) agg;
                value = avg.getValue();
            } else if (agg instanceof Sum) {
                Sum sum = (Sum) agg;
                value = sum.getValue();
            } else if (agg instanceof ValueCount) {
                ValueCount valueCount = (ValueCount) agg;
                value = valueCount.getValue();
            }
            item.put(aggName, value);
        }
        return item;
    }

    private String addTimeFile(String jsonStr) {
        Map<String, Object> map = JsonUtils.toMap(jsonStr);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        map.put(ES_TIMESTAMP_KEY, ISO_FORMATTER.format(now));
        map.put(ES_INSERT_TIME, new Date());
        return JsonUtils.toJSONString(map);
    }

    //判断索引是否存在
    private boolean isIndexExists(String... index) {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
        request.local(false);
        request.humanReadable(true);
        boolean exists = Boolean.FALSE;
        try {
            exists = client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES-isIndexExists发生IO异常，无法获取数据，入参{}", request, e);
            throw new EsException("ES-getById发生IO异常，无法获取数据，入参" + request, e);
        }
        return exists;
    }

}


