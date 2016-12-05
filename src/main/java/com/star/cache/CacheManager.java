package com.star.cache;

import com.star.cache.ehcache.EhCacheProvider;
import com.star.cache.empty.NullCacheProvider;
import com.star.cache.redis.cluster.ClusterRedisCacheProvider;
import com.star.cache.redis.shard.ShardedRedisCacheProvider;
import com.star.cache.redis.single.RedisCacheProvider;
import com.star.config.Config;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 缓存管理器
 * <p>
 * Created by starhq on 2016/12/5.
 */
public final class CacheManager {

    /**
     * 一级缓存提供者
     */
    private static CacheProvider l1Provider;
    /**
     * 二级缓存提供者
     */
    private static CacheProvider l2Provider;

    private CacheManager() {
    }

    /**
     * 初始化缓存提供者
     */
    public static void initCacheProvider() {
        final Config config = new Config("config.properties");
        l1Provider = getProviderInstance(config.getString("ehcache", null));
        l1Provider.start();

        l2Provider = getProviderInstance(config.getString("ehcache", null));
        l2Provider.start();
    }


    /**
     * 根据传入参数获得缓存提供者
     *
     * @param value 缓存提供者名字
     * @return cacheProvider
     */
    private static CacheProvider getProviderInstance(final String value) {
        CacheProvider cacheProvider;
        switch (value) {
            case "ehcache":
                cacheProvider = new EhCacheProvider();
                break;
            case "redis":
                cacheProvider = new RedisCacheProvider();
                break;
            case "sharededredis":
                cacheProvider = new ShardedRedisCacheProvider();
                break;
            case "clusterredis":
                cacheProvider = new ClusterRedisCacheProvider();
                break;
            default:
                cacheProvider = new NullCacheProvider();
                break;
        }
        return cacheProvider;
    }

    /**
     * 获得对应provider的缓存
     *
     * @param level      缓存级别
     * @param cache_name 命名空间
     * @param autoCreate 自动哦你创建，就ehcache游泳
     * @return 缓存实例
     */
    private static Cache getCache(final int level, final String cache_name, final boolean autoCreate) {
        return ((level == 1) ? l1Provider : l2Provider).buildCache(cache_name, autoCreate);
    }

    /**
     * 关闭对应的缓存提供者
     *
     * @param level 缓存级别
     */
    public void shutdown(final int level) {
        ((level == 1) ? l1Provider : l2Provider).stop();
    }

    /**
     * 获得缓存中的值
     *
     * @param level 缓存级别
     * @param name  缓存空间
     * @param key   缓存key
     * @return value
     */
    public static Object get(final int level, final String name, final Serializable key) {
        Object result = null;
        if (!Objects.isNull(key)) {
            final Cache cache = getCache(level, name, false);
            if (!Objects.isNull(cache)) {
                result = cache.get(key);
            }
        }
        return result;
    }


    /**
     * 设置缓存
     *
     * @param level   级别
     * @param name    命名空间
     * @param key     缓存key
     * @param value   缓存value
     * @param seconds 过期时间
     */
    public static void set(final int level, final String name, final Serializable key, final Object value, final int
            seconds) {
        if (!Objects.isNull(key) && !Objects.isNull(value)) {
            final Cache cache = getCache(level, name, true);
            if (cache != null) {
                cache.put(key, value, seconds);
            }
        }
    }

    /**
     * 失效缓存
     *
     * @param level 缓存级别
     * @param name  命名空间
     * @param key   要失效的key
     */
    public static void evict(final int level, final String name, final Serializable key) {
        if (!Objects.isNull(key)) {
            final Cache cache = getCache(level, name, false);
            if (cache != null) {
                cache.evict(key);
            }
        }
    }

    /**
     * 批量失效
     *
     * @param level 缓存级别
     * @param name  命名空间
     * @param keys  失效的keys
     */
    public static void batchEvict(final int level, final String name, final Serializable... keys) {
        if (keys != null && keys.length > 0) {
            final Cache cache = getCache(level, name, false);
            if (cache != null) {
                cache.evict(keys);
            }
        }
    }

    /**
     * 清空缓存
     *
     * @param level 缓存级别
     * @param name  命名空间
     */
    public static void clear(final int level, final String name) {
        final Cache cache = getCache(level, name, false);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 获得缓存的所有键
     *
     * @param level 缓存级别
     * @param name  命名空间
     * @return 键的集合
     */
    public static List<?> keys(final int level, final String name) {
        final Cache cache = getCache(level, name, false);
        return Objects.isNull(cache) ? null : cache.getKeys();
    }
}
