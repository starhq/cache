package com.star.cache.empty;

import com.star.cache.Cache;
import com.star.cache.CacheProvider;
import com.star.cache.exception.CacheException;

/**
 * 空的cacheprovider
 * <p>
 * Created by starhq on 2016/11/25.
 */
public class NullCacheProvider implements CacheProvider {
    @Override
    public String name() {
        return null;
    }

    @Override
    public Cache buildCache(String regionName, boolean autoCreate) throws CacheException {
        return null;
    }

    @Override
    public void start() throws CacheException {

    }

    @Override
    public void stop() {

    }
}
