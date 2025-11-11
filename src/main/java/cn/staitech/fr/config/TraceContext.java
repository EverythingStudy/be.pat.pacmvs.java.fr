package cn.staitech.fr.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * traceId上下文
 */
public class TraceContext {
    private static final String TRACE_ID = "traceId";

    private static final TransmittableThreadLocal<Map<String, String>> traceIdHolder = new TransmittableThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }

        // 创建新的 HashMap 进行拷贝，避免引用传递导致的数据污染
        @Override
        public Map<String, String> copy(Map<String, String> parentValue) {
            return parentValue == null ? new HashMap<>() : new HashMap<>(parentValue);
        }

        @Override
        protected void beforeExecute() {
            // 重要：在线程执行任务前，将 TTL 中的 traceId 设置到当前线程的 MDC
            Map<String, String> context = get();
            if (context != null && context.containsKey(TRACE_ID)) {
                MDC.put(TRACE_ID, context.get(TRACE_ID));
            }
        }

        @Override
        protected void afterExecute() {
            // 任务执行完成后，清理当前线程的 MDC
            MDC.remove(TRACE_ID);
        }
    };

    /**
     * 生成并设置traceId
     */
    public static void generateTraceId() {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        setTraceId(traceId);
    }

    /**
     * 设置traceId到MDC和TransmittableThreadLocal
     */
    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            return;
        }
        Map<String, String> context = traceIdHolder.get();
        context.put(TRACE_ID, traceId);

        // 同时设置到当前线程的 MDC
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 获取当前线程的traceId
     */
    public static String getTraceId() {
        Map<String, String> context = traceIdHolder.get();
        return context.get(TRACE_ID);
    }

    /**
     * 清理traceId
     */
    public static void clear() {
        traceIdHolder.remove();
        MDC.remove(TRACE_ID);
    }
}
