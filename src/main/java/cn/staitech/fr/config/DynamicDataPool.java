package cn.staitech.fr.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
public class DynamicDataPool {
    @Value("${dynamic.corePoolSize}")
    private Integer dynamicCorePoolSize;
    @Value("${dynamic.maxPoolSize}")
    private Integer dynamicMaxPoolSize;
    private ExecutorService dynamicDataThreadPool;

    @Bean("dynamicDataThreadPool")
    public ExecutorService dynamicDataThreadPool() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize * 2;

        this.dynamicDataThreadPool = new ThreadPoolExecutor(dynamicCorePoolSize, dynamicMaxPoolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(200), new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "dynamic-data-thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        return this.dynamicDataThreadPool;
    }

    /**
     * 优雅关闭所有线程池
     */
    @PreDestroy
    public void shutdown() {
        shutdownExecutor(dynamicDataThreadPool, "动态数据线程池");
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null && !executor.isShutdown()) {
            log.info("开始关闭 {}", name);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("{} 未能在 60 秒内关闭，尝试强制关闭", name);
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        log.error("{} 未能正常关闭", name);
                    }
                }
            } catch (InterruptedException e) {
                log.error("等待 {} 关闭时被中断", name, e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("{} 已关闭", name);
        }
    }
    
    /**
     * 新增：任务线程池
     * 策略：低并发、有界队列、快速失败（防止 OOM 和主线程阻塞）
     */
    @Bean("taskExecutor")
    public ThreadPoolExecutor taskExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        
        // 核心逻辑：保持低并发，避免启动过多 子进程耗尽 CPU
        int corePoolSize = Math.max(1, processors / 16);
        int maxPoolSize = Math.max(2, processors / 16);
        
        int queueCapacity = 10000; 

        log.info(">>> 初始化 任务线程池 <<<");
        log.info("CPU 核心数: {}, 核心线程: {}, 最大线程: {}, 队列容量: {}", 
                processors, corePoolSize, maxPoolSize, queueCapacity);

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new NamedThreadFactory("Calc-Thread"),
                (r, executor) -> {
                    String msg = String.format("task 任务被拒绝！[活跃:%d/最大:%d] [队列:%d/%d]",
                            executor.getActiveCount(), executor.getMaximumPoolSize(),
                            executor.getQueue().size(), queueCapacity);
                    log.error(msg);
                    throw new RejectedExecutionException(msg);
                }
        );
    }
    
    static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }
}
