package cn.staitech.fr.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * 请求线程过滤器，用于在日志中添加线程编号
 */
@Component
public class RequestThreadFilter implements Filter {
    private static final TransmittableThreadLocal<String> traceIdHolder = new TransmittableThreadLocal<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 生成线程编号
        String threadId = UUID.randomUUID().toString().substring(0, 8);

        // 将线程编号放入MDC中
        MDC.put("threadId", threadId);
        traceIdHolder.set(threadId);
        try {
            // 继续执行过滤器链
            chain.doFilter(request, response);
        } finally {
            // 清除MDC中的线程编号，避免内存泄漏
            MDC.remove("threadId");
            traceIdHolder.remove();
        }
    }
}