package com.fxx.common.web.config;

import com.fxx.common.web.filter.ChannelFilter;
import com.fxx.common.web.filter.XssFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

/**
 * Filter配置
 */
@Configuration
@Slf4j
public class FilterAutoConfigurer {
    @Bean
    public Filter xssFilter() {
        return new XssFilter();
    }

    @Bean
    public Filter channelFilter() {
        return new ChannelFilter();
    }

    @Bean
    public FilterRegistrationBean channelFilterRegistration() {
        log.info("ChannelFilter开始注册！");
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(channelFilter());
        registration.addUrlPatterns("/*");
        registration.setName("ChannelFilter");
        registration.setOrder(1);
        log.info("ChannelFilter注册完成！");
        return registration;
    }

    @Bean
    public FilterRegistrationBean xssFilterRegistration() {
        log.info("XssFilter开始注册！");
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(xssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(2);
        log.info("XssFilter注册完成！");
        return registration;
    }


}
