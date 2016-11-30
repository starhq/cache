package com.star.cache.redis.single;

import com.star.cache.Cache;
import com.star.cache.CacheProvider;
import com.star.cache.exception.CacheException;
import com.star.config.Config;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by starhq on 2016/11/28.
 */
public class RedisCacheProvider implements CacheProvider {

    private JedisPool pool;

    private ConcurrentHashMap<String, RedisCache> redisMap = new ConcurrentHashMap<>();

    @Override

    public String name() {
        return "redis";
    }

    @Override
    public Cache buildCache(String regionName, boolean autoCreate) throws CacheException {
        RedisCache cache = redisMap.get(regionName);
        if (Objects.isNull(cache)) {
            cache = new RedisCache(regionName, pool);
            redisMap.put(regionName, cache);
        }
        return cache;
    }

    @Override
    public void start() throws CacheException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        Config config = new Config("config.properties");
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        poolConfig.setBlockWhenExhausted(config.getBool("redis.blockWhenExhausted", true));
        //最大空闲连接数
        poolConfig.setMaxIdle(config.getInt("redis.maxIdle", 10));
        //最小空闲连接数
        poolConfig.setMinIdle(config.getInt("redis.minIdle", 5));
        //最大连接数
        poolConfig.setMaxTotal(config.getInt("redis.maxTotal", 10000));
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        poolConfig.setMaxWaitMillis(config.getLong("redis.maxWaitMillis", 100));
        //在空闲时检查有效性, 默认false
        poolConfig.setTestWhileIdle(config.getBool("redis.testWhileIdle", false));
        //在获取连接的时候检查有效性, 默认false
        poolConfig.setTestOnBorrow(config.getBool("redis.testOnBorrow", true));
        //返回连接时，检测连接是否成功
        poolConfig.setTestOnReturn(config.getBool("redis.testOnReturn", false));
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        poolConfig.setNumTestsPerEvictionRun(config.getInt("redis.numTestsPerEvictionRun", 10));
        //连接空闲的最小时间，达到此值后空闲连接将可能会被移除。负值(-1)表示不移除。
        poolConfig.setMinEvictableIdleTimeMillis(config.getLong("redis.minEvictableIdleTimeMillis", 1000));
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        poolConfig.setSoftMinEvictableIdleTimeMillis(config.getLong("redis.minEvictableIdleTimeMillis", 10));
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getLong("redis.timeBetweenEvictionRunsMillis", 10));
        //是否启用后进先出, 默认true
        poolConfig.setLifo(config.getBool("redis.lifo", false));


        pool = new JedisPool(poolConfig, config.getString("redis.host", "localhost"), config.getInt("redis" +
                ".port", 6379), config.getInt("redis.timeout", 2000), config.getString("redis.password",
                null), config.getInt("redis.database", 0));
    }

    @Override
    public void stop() {
        pool.destroy();
        redisMap.clear();
    }
}
