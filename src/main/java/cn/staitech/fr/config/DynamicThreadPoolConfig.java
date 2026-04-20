package cn.staitech.fr.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class DynamicThreadPoolConfig {
    @Bean
    public ExecutorService executorService() {
        log.info("系统CPU核心数识别为: {}", Runtime.getRuntime().availableProcessors());
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5), new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                int num = threadNumber.getAndIncrement();
                log.info("创建JsonTask任务线程: {}", num);
                Thread thread = new Thread(r, "json-task-thread-" + num);
                thread.setDaemon(false);
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());

        // 启用线程池监控
        executor.allowCoreThreadTimeOut(false);
        return executor;
    }
}
