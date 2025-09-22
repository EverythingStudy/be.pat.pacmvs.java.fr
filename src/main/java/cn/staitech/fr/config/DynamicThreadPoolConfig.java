package cn.staitech.fr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class DynamicThreadPoolConfig {
    private static final int CORE_POOL_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors());
    private static final int MAX_POOL_SIZE = Math.max(10, Runtime.getRuntime().availableProcessors() * 2);

    @Bean
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
