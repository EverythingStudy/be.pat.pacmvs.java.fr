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
    //ai json文件分割线程池调整  核心倍数和最大倍数的调整
    @Value("${dynamic.coreMultiple}")
    private Integer dynamicCoreMultiple;
    @Value("${dynamic.maxMultiple}")
    private Integer dynamicMaxMultiple;
    
    private ExecutorService dynamicDataThreadPool;
    
    private ThreadPoolExecutor recognitionExecutor; 

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
     * 关闭所有线程池
     */
    @PreDestroy
    public void shutdown() {
    	shutdownExecutor(dynamicDataThreadPool, "动态数据线程池");
        shutdownExecutor(recognitionExecutor, "识别任务线程池");
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
     * 【新增】自定义拒绝策略：用于识别任务
     * 记录详细日志并抛出异常，让调用者感知任务提交失败（以便处理 CountDownLatch）
     */
    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            String msg = String.format("识别任务被拒绝！[当前活跃线程数:%d/最大允许线程数:%d] [队列中任务数:%d/队列剩余容量:%d] 任务:%s",
                    executor.getActiveCount(), 
                    executor.getMaximumPoolSize(),
                    executor.getQueue().size(), 
                    executor.getQueue().remainingCapacity(),
                    r.toString());
            
            log.error(msg);
            // 抛出异常，业务代码 catch 后需执行 countDownLatch.countDown()
            throw new RejectedExecutionException(msg);
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

        log.info(">>> 初始化 [任务线程池]<<<");
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
    
    
    /**
     * 【新增】文件识别任务线程池
     * 用途：用于 submitPathList 中的大量文件 IO 和几何计算任务
     * 策略：IO 密集型 (CPU * 2 ~ CPU * 4)，有界队列，失败快速抛出异常
     */
    @Bean("recognitionExecutor")
    public ThreadPoolExecutor recognitionExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        
        int defaultCore = Math.max(1, processors * dynamicCoreMultiple);
        int defaultMax = Math.max(defaultCore, processors * dynamicMaxMultiple);
        
        int corePoolSize;
        if (dynamicCorePoolSize != null && dynamicCorePoolSize > 0) {
            // 【兜底策略】如果配置值小于 CPU 核心数 (或者小于默认值的 50%)，视为配置不合理，强制使用默认值
            if (dynamicCorePoolSize < processors) {
                log.warn("检测到 [核心线程数] 配置过小 (配置:{}, CPU:{}), 存在性能风险！已自动修正为默认值: {}", 
                        dynamicCorePoolSize, processors, defaultCore);
                corePoolSize = defaultCore;
            } else {
                corePoolSize = dynamicCorePoolSize;
            }
        } else {
            corePoolSize = defaultCore;
        }
        
        // --- 修改最大线程数逻辑 ---
        int maxPoolSize;
        if (dynamicMaxPoolSize != null && dynamicMaxPoolSize > 0) {
            if (dynamicMaxPoolSize < processors || dynamicMaxPoolSize < corePoolSize) {
                log.warn("检测到 [最大线程数] 配置过小 (配置:{}, CPU:{}, 核心线程:{}), 存在性能风险！已自动修正为默认值: {}", 
                        dynamicMaxPoolSize, processors, corePoolSize, defaultMax);
                maxPoolSize = defaultMax;
            } else {
                maxPoolSize = dynamicMaxPoolSize;
            }
        } else {
            maxPoolSize = defaultMax;
        }
        
        int queueCapacity = 15000;

        log.info(">>> 初始化 [识别任务线程池] <<<");
        log.info("CPU 核心数: {}, 核心线程: {}, 最大线程: {}, 队列容量: {}", 
                processors, corePoolSize, maxPoolSize, queueCapacity);

        this.recognitionExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("Recognition-Thread"),
                new CustomRejectedExecutionHandler() // 使用自定义拒绝策略
        );
        
        return this.recognitionExecutor;
    }
}
