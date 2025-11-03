package cn.staitech.fr.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义拒绝策略 - 结合实际业务需求
 */
@Data
@Slf4j
public class CustomRejectionPolicies {

    /**
     * 记录日志的拒绝策略
     */
    public static class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
        private static final Logger logger = LoggerFactory.getLogger(LoggingRejectedExecutionHandler.class);

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 记录详细的拒绝信息
            logger.warn("任务被线程池拒绝: " + "池大小: {}/{}, 活跃线程: {}, 队列大小: {}/{}, 已完成任务: {}", executor.getPoolSize(), executor.getMaximumPoolSize(), executor.getActiveCount(), executor.getQueue().size(), executor.getQueue().remainingCapacity(), executor.getCompletedTaskCount());

            // 可以选择抛出异常或执行其他逻辑
            throw new RejectedExecutionException("任务被线程池拒绝，当前线程池已满");
        }
    }

    /**
     * 等待重试的拒绝策略
     */
    public static class RetryRejectedExecutionHandler implements RejectedExecutionHandler {
        private final long maxWaitTimeMs;
        private final long retryIntervalMs;

        public RetryRejectedExecutionHandler(long maxWaitTimeMs, long retryIntervalMs) {
            this.maxWaitTimeMs = maxWaitTimeMs;
            this.retryIntervalMs = retryIntervalMs;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < maxWaitTimeMs) {
                try {
                    // 尝试重新提交
                    executor.getQueue().offer(r, retryIntervalMs, TimeUnit.MILLISECONDS);
                    if (executor.getQueue().contains(r)) {
                        return; // 成功入队
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RejectedExecutionException("重试被中断", e);
                }
            }

            // 超时后执行降级策略
            executeFallback(r);
        }

        private void executeFallback(Runnable r) {
            // 降级策略：在调用者线程执行或记录到数据库等
            log.warn("任务重试超时，执行降级策略");
            r.run();
        }
    }

    /**
     * 降级策略处理器
     */
    public static class FallbackRejectedExecutionHandler implements RejectedExecutionHandler {
        private final RejectedExecutionHandler fallbackHandler;

        public FallbackRejectedExecutionHandler(RejectedExecutionHandler fallbackHandler) {
            this.fallbackHandler = fallbackHandler;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("线程池饱和，执行降级策略");

            // 执行降级逻辑
            if (r instanceof FallbackRunnable) {
                ((FallbackRunnable) r).fallback();
            } else {
                fallbackHandler.rejectedExecution(r, executor);
            }
        }

        public interface FallbackRunnable extends Runnable {
            void fallback();
        }
    }
}