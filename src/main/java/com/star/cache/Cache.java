package com.star.cache;

import com.star.cache.exception.CacheException;

import java.io.Serializable;
import java.util.List;

/**
 * 借鉴了j2cache
 * <p>
 * Created by starhq on 2016/11/24.
 */
public interface Cache {

    /**
     * 缓存中获取值
     *
     * @param key 键
     * @return value
     * @throws CacheException 缓存异常
     */
    Object get(final Serializable key) throws CacheException;

    /**
     * 存入缓存，也做更新用
     *
     * @param key     键
     * @param value   值
     * @param seconds 超时时间
     * @throws CacheException 缓存异常
     */
    void put(final Serializable key, final Object value, final int seconds) throws CacheException;

    /**
     * 更新缓存，也做更新用
     *
     * @param key   键
     * @param value 值
     * @throws CacheException 缓存异常
     */
    void update(final Serializable key, final Object value, final int seconds) throws CacheException;

    /**
     * 按key清除缓存
     *
     * @param key 键
     * @throws CacheException 缓存异常
     */
    void evict(final Serializable key) throws CacheException;

    /**
     * 按key批量清除缓存
     *
     * @param keys 键的集合
     * @throws CacheException 缓存异常
     */
    void evict(final Serializable... keys) throws CacheException;

    /**
     * 清空缓存
     *
     * @throws CacheException 缓存异常
     */
    void clear() throws CacheException;

    /**
     * 做些收尾
     *
     * @throws CacheException 缓存异常
     */
    void destory() throws CacheException;

    /**
     * 获得键的集合
     *
     * @param name 空间？像echache中getCache中的值
     * @return 键的集合
     * @throws CacheException 缓存异常
     */
    List<?> getKeys() throws CacheException;

    /**
     * key是否存在
     *
     * @param key 键数组
     * @return 是否存在
     * @throws CacheException
     */
    boolean exists(final Serializable key) throws CacheException;
}
