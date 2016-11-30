package com.star.cache;

import com.star.cache.exception.CacheException;

/**
 * 缓存提供者
 * <p>
 * Created by starhq on 2016/11/25.
 */
public interface CacheProvider {

    /**
     * 用的是哪种cache
     *
     * @return
     */
    String name();

    /**
     * 构建缓存
     *
     * @param regionName 缓存区  类似于ehcache配置文件中的cachename
     * @param autoCreate 自动
     * @return
     */
    Cache buildCache(final String regionName, final boolean autoCreate) throws CacheException;

    /**
     * 启动缓存
     */
    void start() throws CacheException;

    /**
     * 停止缓存
     */
    void stop();
}
