package cn.staitech.fr.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisClientUtil implements ICache {
    @Resource(name = "redissonClient")
    private RedissonClient client;

    @Override
    public boolean removeList(String key) {
        return client.getList(key).delete();
    }

    @Override
    public Long getAndAddLong(String key, Long delta) {
        return client.getAtomicLong(key).addAndGet(delta);
    }

    @Override
    public boolean setString(String key, String value) {
        try {
            RBucket<String> rBucket = client.getBucket(key);
            rBucket.set(value, 30, TimeUnit.MINUTES);
            // 设置有效时间为30分钟
            return true;
        } catch (Exception ex) {
            log.error("", ex);
            return false;
        }
    }

    @Override
    public Object getString(String key) {
        return client.getBucket(key).get();
    }


    @Override
    public boolean delString(String key) {
        return client.getKeys().delete(key) > 0;
    }


    @Override
    public boolean setHash(String slideId, Long userId, String userName) {
        RMap<Object, Object> rMap = client.getMap(slideId);
        rMap.put("userId", userId);
        rMap.put("userName", userName);
        rMap.expire(30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public boolean updateHashTime(String res) {
        RMap<Object, Object> rMap = client.getMap(res);
        rMap.expire(30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public RMap<Object, Object> getHash(String res) {
        return client.getMap(res);
    }

    @Override
    public boolean acquire(String req) {
        RLock rLock = client.getLock(req);
        rLock.lock();
        try {
            // 尝试5秒内获取锁，如果获取到了，最长60秒自动释放
            boolean res = rLock.tryLock(5, TimeUnit.MINUTES);
            if (res) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("获取锁失败，失败原因：" + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean release(String lockKey) {
        RLock rLock = client.getLock(lockKey);
        try {
            //尝试5秒内获取锁，如果获取到了，最长60秒自动释放
            boolean res = rLock.tryLock(5, TimeUnit.MINUTES);
            if (res) {
                // 进行锁释放
                rLock.unlock();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("获取锁失败，失败原因：" + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean putObjectAllToMap(String name, Map<String, Object> value, long cacheTime) {
        try {
            RMap<String, Object> rmap = client.getMap(name);
            rmap.putAll(value);
            if (cacheTime > 0) {
                client.getMap(name).expire(cacheTime, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception ex) {
            log.error("", ex);
            return false;
        }
    }

    @Override
    public void removeKeysFromMap(String name, String[] keys) {
        RMap rmap = client.getMap(name);
        if (rmap != null) {
            rmap.fastRemove(keys);
        }

    }

    @Override
    public Set<Map.Entry<Object, Object>> getAllFromMap(String name) {
        RMap rmap = client.getMap(name);
        if (rmap != null) {
            return new LinkedHashSet<Map.Entry<Object, Object>>(rmap.entrySet());
        }
        return null;
    }
}