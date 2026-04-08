package cn.staitech.fr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DynamicThreadPoolConfig {
    @Bean
    public ExecutorService executorService() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(5), new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "dynamic-pool-thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());

        // 启用线程池监控
        executor.allowCoreThreadTimeOut(false);
        return executor;
    }
}
