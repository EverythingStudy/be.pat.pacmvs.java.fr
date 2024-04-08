package cn.staitech.fr.service;

import cn.staitech.fr.utils.RocksDBUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * @author: wangfeng
 * @create: 2024-02-20 18:11:25
 * @Description: 会话
 */
@Slf4j
@Service
public class RocksdbService {

    public static final ExecutorService rocksdbExecutorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            // 空闲线程等待工作的超时时间
            0,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(4096),
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(r, "history-service-thread-" + r.hashCode());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());


    public  <T> void submitTask(String cfName, String key, T obj) {
        rocksdbExecutorService.submit(new SaveRocksdbRunnable(cfName, key, obj));
    }

    class SaveRocksdbRunnable<T> implements Runnable {
        private final String cfName;
        private final String key;
        private final T obj;

        SaveRocksdbRunnable(String cfName, String key, T obj) {
            this.cfName = cfName;
            this.key = key;
            this.obj = obj;
        }

        @Override
        public void run() {
            try {
                // 3、数据持久化写入RocksDB
                Gson gson = new Gson();
                // 将对象转换成JSON字符串
                String json = gson.toJson(obj);
                RocksDBUtil.put(cfName, key, json);
            } catch (Exception e) {
                log.info("saveRocksDB:{}", e);
            }

        }
    }


}
