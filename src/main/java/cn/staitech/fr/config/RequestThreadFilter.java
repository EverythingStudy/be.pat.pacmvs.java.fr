package cn.staitech.fr.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * 请求线程过滤器，用于在日志中添加线程编号
 */
@Component
public class RequestThreadFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            TraceContext.generateTraceId();
            // 继续执行过滤器链
            chain.doFilter(request, response);
        } finally {
            // 清除MDC中的线程编号，避免内存泄漏
            TraceContext.clear();
        }
    }
}