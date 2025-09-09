package cn.staitech.fr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestThreadFilter> requestThreadFilterRegistration() {
        FilterRegistrationBean<RequestThreadFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestThreadFilter());
        registration.addUrlPatterns("/*");
        registration.setName("requestThreadFilter");
        registration.setOrder(1);
        return registration;
    }
}