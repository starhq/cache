package com.star.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 给外部调用
 * <p>
 * Created by starhq on 2016/12/5.
 */
public class CacheToolkit {

    /**
     * 一级缓存常量
     */
    private static final int LEVEL_1 = 1;
    /**
     * 二级缓存常量
     */
    private static final int LEVEL_2 = 2;

    /**
     * 私有构造方法
     */
    private CacheToolkit() {
        CacheManager.initCacheProvider();
    }

    /**
     * 单例持有对象的引用
     */
    private static class CacheToolkitHolder {
        /**
         * 初始化单例
         */
        public static final CacheToolkit INSTANCE = new CacheToolkit();
    }

    /**
     * 单例获取对象
     *
     * @return 单例
     */
    public static CacheToolkit getInstance() {
        return CacheToolkitHolder.INSTANCE;
    }

    /**
     * 获取缓存中的数据
     *
     * @param region : 民命空间
     * @param key    : 缓存key
     * @return value
     */
    public Object get(final String region, final Serializable key) {
        Object result = null;
        if (!Objects.isNull(key)) {
            result = CacheManager.get(LEVEL_1, region, key);
            if (Objects.isNull(result)) {
                result = CacheManager.get(LEVEL_2, region, key);
                if (!Objects.isNull(result)) {
                    CacheManager.set(LEVEL_1, region, key, result, 0);
                }
            }
        }
        return result;
    }

    /**
     * 设置缓存
     *
     * @param region 命名空间
     * @param key    键
     * @param value  值
     */
    public void set(final String region, final Serializable key, final Object value, final int seconds) {
        if (!Objects.isNull(key)) {
            if (value == null) {
                evict(region, key);
            } else {
                CacheManager.set(LEVEL_1, region, key, value, seconds);
                CacheManager.set(LEVEL_2, region, key, value, seconds);
            }
        }
    }

    /**
     * 删除缓存
     *
     * @param region 命名空间
     * @param key    键
     */
    private void evict(final String region, final Serializable key) {
        CacheManager.evict(LEVEL_1, region, key);
        CacheManager.evict(LEVEL_2, region, key);
    }

    /**
     * 批量删除缓存
     *
     * @param region 命名空间
     * @param keys   键
     */
    private void evict(final String region, final Serializable... keys) {
        CacheManager.evict(LEVEL_1, region, keys);
        CacheManager.evict(LEVEL_2, region, keys);
    }

    /**
     * 清空缓存
     *
     * @param region : 命名空间
     */
    public void clear(final String region) {
        CacheManager.clear(LEVEL_1, region);
        CacheManager.clear(LEVEL_2, region);
    }


    /**
     * 获得所有缓存的键
     *
     * @param region 命名空间
     * @return 键的集合
     */
    public List<?> keys(final String region) {
        return CacheManager.keys(LEVEL_1, region);
    }
}
