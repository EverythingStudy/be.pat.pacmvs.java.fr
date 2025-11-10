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

    @PreDestroy
    public void shutdown() {
        if (dynamicDataThreadPool != null && !dynamicDataThreadPool.isShutdown()) {
            log.info("开始关闭动态数据处理线程池");
            dynamicDataThreadPool.shutdown();
            try {
                if (!dynamicDataThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("动态数据处理线程池未能在60秒内关闭，尝试强制关闭");
                    dynamicDataThreadPool.shutdownNow();
                    if (!dynamicDataThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        log.error("动态数据处理线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                log.error("等待线程池关闭时被中断", e);
                dynamicDataThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("动态数据处理线程池已关闭");
        }
    }
}
