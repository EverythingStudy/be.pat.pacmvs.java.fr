package cn.staitech.fr.config;

import org.redisson.api.RMap;

import java.util.Map;
import java.util.Set;

public interface ICache {
    /**
     * 清空整个List
     *
     * @param key
     * @return
     */
    boolean removeList(String key);

    /**
     * 对指定的key值进行自增操作，并且返回自增前的值
     *
     * @param key
     * @param delta
     * @return
     */
    Long getAndAddLong(String key, Long delta);

    /**
     * 添加字符串类型数据
     *
     * @param key
     * @param delta
     * @return
     */
    boolean setString(String key, String value);

    /**
     * 根据键获取指定的值
     *
     * @param key 键
     * @return Object 值
     */
    Object getString(String key);

    /**
     * 根据键删除值
     *
     * @param key
     * @param delta
     * @return
     */
    boolean delString(String key);

    /**
     * 添加hash
     *
     * @param key
     * @param delta
     * @return
     */
    boolean setHash(String slideId, Long userId, String userName);

    /**
     * 更新key过期时间
     *
     * @param res
     * @return
     */
    boolean updateHashTime(String res);

    /**
     * 获取hash
     *
     * @param res
     * @return
     */
    RMap<Object, Object> getHash(String res);

    /**
     * 加锁
     *
     * @param req
     * @return
     */
    boolean acquire(String req);

    /**
     * 释放锁
     *
     * @param req
     * @return
     */
    boolean release(String lockKey);

    /**
     * 向指定的Map集合中添加元素，已有则覆盖
     *
     * @param name      Map名称
     * @param value
     * @param cacheTime
     * @return
     */
    boolean putObjectAllToMap(String name, Map<String, Object> value, long cacheTime);

    /**
     * 删除Map中指定的key
     *
     * @param name
     * @param key
     */
    void removeKeysFromMap(String name, String[] key);

    /**
     * 获得Map中所有key-value对
     *
     * @param name
     * @return
     */
    Set<Map.Entry<Object, Object>> getAllFromMap(String name);
}