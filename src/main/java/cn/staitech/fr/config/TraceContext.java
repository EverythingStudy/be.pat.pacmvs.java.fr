package cn.staitech.fr.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.jboss.logging.MDC;

import java.util.UUID;

public class TraceContext {
    private static final String TRACE_ID = "traceId";
    private static final TransmittableThreadLocal<String> traceIdHolder = new TransmittableThreadLocal<>();

    /**
     * 生成并设置traceId
     */
    public static void generateTraceId() {
        String traceId =  UUID.randomUUID().toString().substring(0, 8);
        setTraceId(traceId);
    }

    /**
     * 设置traceId到MDC和TransmittableThreadLocal
     */
    public static void setTraceId(String traceId) {
        traceIdHolder.set(traceId);
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 获取当前线程的traceId
     */
    public static String getTraceId() {
        return traceIdHolder.get();
    }

    /**
     * 清理traceId
     */
    public static void clear() {
        traceIdHolder.remove();
        MDC.remove(TRACE_ID);
    }
}
