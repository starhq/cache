package com.star.cache.ehcache;

import com.star.cache.Cache;
import com.star.cache.exception.CacheException;
import com.star.collection.ArrayUtil;
import com.star.collection.CollectionUtil;
import com.star.string.StringUtil;
import net.sf.ehcache.Element;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ehcache缓存
 * <p>
 * Created by starhq on 2016/11/25.
 */
public class Ehcache implements Cache {

    /**
     * ehcache的缓存对象
     */
    private final net.sf.ehcache.Cache cache;

    /**
     * 构造方法
     *
     * @param cache 缓存对象
     */
    public Ehcache(final net.sf.ehcache.Cache cache) {
        this.cache = cache;
    }

    /**
     * 从缓存中取值
     *
     * @param key 键
     * @return value
     * @throws CacheException 缓存异常
     */
    @Override
    public Object get(final Serializable key) throws CacheException {
        try {
            Object result;
            if (Objects.isNull(key)) {
                result = null;
            } else {
                final Element element = cache.get(key);
                result = Objects.isNull(element) ? null : element.getObjectValue();
            }

            return result;
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("get value from cache failure: {}", e.getMessage()), e);
        }

    }

    /**
     * 数据缓存
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间
     * @throws CacheException 缓存异常
     */
    @Override
    public void put(final Serializable key, final Object value, final int seconds) throws CacheException {
        try {
            final Element element = new Element(key, value);
            if (seconds > 0) {
                element.setTimeToLive(seconds);
            }
            cache.put(element);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("put value into cache failure: {}", e.getMessage()), e);
        }

    }

    /**
     * 更新缓存
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间
     * @throws CacheException 缓存异常
     */
    @Override
    public void update(final Serializable key, final Object value, final int seconds) throws CacheException {
        try {
            final Element element = new Element(key, value);
            if (seconds > 0) {
                element.setTimeToLive(seconds);
            }
            cache.replace(element);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("replace value in cache failure: {}", e.getMessage()), e);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @throws CacheException 缓存异常
     */
    @Override
    public void evict(final Serializable key) throws CacheException {
        try {
            cache.remove(key);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("remove value from cache failure: {}", e.getMessage()), e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键的集合
     * @throws CacheException 缓存异常
     */
    @Override
    public void evict(final Serializable... keys) throws CacheException {
        try {
            cache.removeAll(CollectionUtil.wrapHashSet(keys));
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("batch remove value from cache failure: {}", e.getMessage()),
                    e);
        }
    }

    /**
     * 清空所有缓存
     *
     * @throws CacheException 缓存异常
     */
    @Override
    public void clear() throws CacheException {
        try {
            cache.removeAll();
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("clear cache failure: {}", e.getMessage()), e);
        }
    }

    /**
     * 销毁缓存
     *
     * @throws CacheException 缓存异常
     */
    @Override
    public void destory() throws CacheException {
        try {
            cache.getCacheManager().removeCache(cache.getName());
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("destory cache failure: {}", e.getMessage()), e);
        }
    }

    /**
     * 获得所有键
     *
     * @return 键的集合
     * @throws CacheException 缓存异常
     */
    @Override
    public List<?> getKeys() throws CacheException {
        try {
            return cache.getKeys();
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("get keys is in cache failure: {}", e.getMessage
                    ()), e);
        }
    }

    /**
     * 键是否存在
     *
     * @param key 键数组
     * @return 是否存在
     * @throws CacheException 缓存异常
     */
    @Override
    public boolean exists(final Serializable key) throws CacheException {
        try {
            return cache.isKeyInCache(key);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(StringUtil.format("detect wheather key is in cache failure: {}", e.getMessage
                    ()), e);
        }
    }
}
