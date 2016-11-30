package com.star.cache.ehcache;

import com.star.cache.Cache;
import com.star.cache.CacheProvider;
import com.star.cache.exception.CacheException;
import com.star.clazz.ClassUtil;
import com.star.io.file.PathUtil;
import com.star.string.StringUtil;
import net.sf.ehcache.CacheManager;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ehcache提供者
 * <p>
 * Created by starhq on 2016/11/25.
 */
public class EhCacheProvider implements CacheProvider {

    /**
     * ehcache的缓存管理器
     */
    private CacheManager cacheManager;

    /**
     * 缓存ehcache
     */
    private ConcurrentHashMap<String, Ehcache> ehcacheMap;

    /**
     * 缓存提供者
     *
     * @return 提供者
     */
    @Override
    public String name() {
        return "ehcache";
    }

    /**
     * 构建缓存
     *
     * @param regionName 缓存区  类似于ehcache配置文件中的cachename
     * @param autoCreate 自动
     * @return 缓存
     * @throws CacheException 缓存异常
     */
    @Override
    public Cache buildCache(final String regionName, final boolean autoCreate) throws CacheException {
        Ehcache ehcache = ehcacheMap.get(regionName);
        if (Objects.isNull(ehcache) && autoCreate) {
            synchronized (ehcacheMap) {
                ehcache = ehcacheMap.get(regionName);
                if (Objects.isNull(ehcache)) {
                    net.sf.ehcache.Cache cache = cacheManager.getCache(regionName);
                    if (Objects.isNull(cache)) {
                        cacheManager.addCache(regionName);
                        cache = cacheManager.getCache(regionName);
                    }
                    ehcache = new Ehcache(cache);
                    ehcacheMap.put(regionName, ehcache);
                }
            }
        }
        return ehcache;
    }

    /**
     * 启动cachemanager，初始化cachemap
     *
     * @throws CacheException 缓存异常
     */
    @Override
    public void start() throws CacheException {
        if (Objects.isNull(cacheManager)) {
            try {
                final Path path = Paths.get(ClassUtil.getURL("ehcache.xml").toURI());

                if (PathUtil.exist(path)) {
                    cacheManager = new CacheManager(path.toAbsolutePath().toString());
                } else {
                    cacheManager = CacheManager.getInstance();
                }
            } catch (URISyntaxException e) {
                throw new CacheException(StringUtil.format("ehcache file convert to uri failure: {}", e.getMessage())
                        , e);
            }


        }

        ehcacheMap = new ConcurrentHashMap<>();
    }

    /**
     * 停止服务，清空map
     */
    @Override
    public void stop() {
        if (Objects.isNull(cacheManager)) {
            return;
        }
        cacheManager.shutdown();
        ehcacheMap.clear();
        cacheManager = null;
    }
}
