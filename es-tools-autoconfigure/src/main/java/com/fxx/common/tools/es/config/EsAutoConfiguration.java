
package com.fxx.common.tools.es.config;

import com.fxx.common.tools.es.CommonEsClient;
import com.fxx.common.tools.es.CommonEsClientInter;
import com.fxx.common.tools.utils.StrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.UnknownHostException;


/**
 * @author wangxiao1
 * @date 2019/12/1211:46
 */
@Configuration
@ConditionalOnProperty(
        prefix = "spring.data.elasticsearch",
        value = {"enabled"},
        havingValue = "true"
)
@ConditionalOnClass(name = {"org.elasticsearch.client.RestHighLevelClient"})
@Slf4j
public class EsAutoConfiguration implements InitializingBean {
    /**
     * 解决ES同其他启动netty通信的组件冲突
     */
    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("es_config");
        log.info("es.set.netty.runtime.available.processors:{}", System.getProperty("es.set.netty.runtime.available.processors"));
    }

    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNode;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    @Value("${spring.data.elasticsearch.cluster-psw}")
    private String clusterPsw;

    @Value("${spring.data.elasticsearch.cluster-user}")
    private String clusterUser;

    @Value("${spring.data.elasticsearch.prefix:}")
    private String indexPrfix;

    @Value("${spring.data.elasticsearch.log.enabled:false}")
    private Boolean esQueryLog;

    static final String COLON = ":";


    @Bean
    public RestHighLevelClient client() throws UnknownHostException {
        log.info("EsTransportClient初始化,开始");
        /*
        ES的TCP端口为9300,而不是HTTP端口9200
        这里只配置了一个节点的地址然添加进去,也可以配置多个从节点添加进去再返回
         */
        String hostName = StrUtils.subBefore(clusterNode, COLON, Boolean.TRUE);
        String port = StrUtils.subAfter(clusterNode, COLON, Boolean.TRUE);
        /*用户认证对象*/
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        /*设置账号密码*/
        if (StrUtils.isNotBlank(clusterUser) && StrUtils.isNotBlank(clusterPsw)) {
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(clusterUser, clusterPsw));
        }
        /*创建rest client对象*/
        RestClientBuilder builder = RestClient.builder(new HttpHost(hostName, Integer.valueOf(port)))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        log.info("EsTransportClient初始化,完成");
        return client;
    }

    @Bean
    public CommonEsClientInter commonEsClient(RestHighLevelClient client) throws UnknownHostException {
        log.info("CommonEsClient初始化,开始");
        CommonEsClientInter commonEsClient = new CommonEsClient(client, esQueryLog, indexPrfix);
        log.info("CommonEsClient初始化,完成");
        return commonEsClient;
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        log.info("EsTransportClient销毁，开始");
        RestHighLevelClient client = client();
        if (client != null) {
            client.close();
        }
        log.info("EsTransportClient销毁，完成");
    }

}
