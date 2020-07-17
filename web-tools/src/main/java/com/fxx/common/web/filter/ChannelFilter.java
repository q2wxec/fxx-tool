
package com.fxx.common.web.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

import com.fxx.common.tools.utils.JsonUtils;
import com.fxx.common.tools.utils.StrUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @date 2020/4/1312:59
 */
@Slf4j
public class ChannelFilter implements Filter {

    public static final String REQUEST_ID = "requestId";


    @Override
    public void init(FilterConfig filterConfig){
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ServletRequest requestWrapper = servletRequest;
        if(servletRequest instanceof HttpServletRequest) {
            // 设置requestId
            String requestId = ((HttpServletRequest) servletRequest).getHeader(REQUEST_ID);
            if (StrUtils.isBlank(requestId)) {
                requestId = UUID.randomUUID().toString().replace("-", "");
            }
            MDC.put(REQUEST_ID, requestId);

            // 包装
            requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);

            // 打印http请求
            log.info("Http RequestURL : {}, Method : {}, RequestParam : {}, RequestBody : {}",
                    ((HttpServletRequest)servletRequest).getRequestURL(),
                    ((HttpServletRequest)servletRequest).getMethod(),
                    JsonUtils.toJSONString(servletRequest.getParameterMap()),
                    ((RequestWrapper) requestWrapper).getBody());
        }
        filterChain.doFilter(requestWrapper, servletResponse);

        // 删除requestId
        MDC.remove(REQUEST_ID);
    }

    @Override
    public void destroy() {
    }
}
