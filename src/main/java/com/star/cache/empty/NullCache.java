package com.star.cache.empty;

import com.star.cache.Cache;
import com.star.cache.exception.CacheException;

import java.io.Serializable;
import java.util.List;

/**
 * 空的缓存实现
 * <p>
 * Created by starhq on 2016/11/25.
 */
public class NullCache implements Cache {
    @Override
    public Object get(Serializable key) throws CacheException {
        return null;
    }

    @Override
    public void put(Serializable key, Object value, int seconds) throws CacheException {

    }

    @Override
    public void update(Serializable key, Object value, int seconds) throws CacheException {

    }

    @Override
    public void evict(Serializable key) throws CacheException {

    }

    @Override
    public void evict(Serializable... keys) throws CacheException {

    }

    @Override
    public void clear() throws CacheException {

    }

    @Override
    public void destory() throws CacheException {

    }

    @Override
    public List<?> getKeys() throws CacheException {
        return null;
    }

    @Override
    public boolean exists(Serializable key) throws CacheException {
        return false;
    }

}
