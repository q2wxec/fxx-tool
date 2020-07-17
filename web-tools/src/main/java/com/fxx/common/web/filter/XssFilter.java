package com.fxx.common.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * XSS过滤
 */
@Slf4j
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        ServletRequest wrapper = servletRequest;
        if (servletRequest instanceof HttpServletRequest) {
            wrapper = new XssHttpServletRequestWrapper((HttpServletRequest) servletRequest);
        }
        filterChain.doFilter(wrapper, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
