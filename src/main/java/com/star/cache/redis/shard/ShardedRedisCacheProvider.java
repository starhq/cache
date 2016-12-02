package com.star.cache.redis.shard;

import com.star.cache.Cache;
import com.star.cache.CacheProvider;
import com.star.cache.exception.CacheException;
import com.star.cache.redis.single.RedisCache;
import com.star.collection.CollectionUtil;
import com.star.config.Config;
import com.star.string.StringUtil;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by starhq on 2016/11/28.
 */
public class ShardedRedisCacheProvider implements CacheProvider {

    private ShardedJedisPool pool;

    private ConcurrentHashMap<String, ShardedRedisCache> redisMap = new ConcurrentHashMap<>();

    @Override

    public String name() {
        return "sharededredis";
    }

    @Override
    public Cache buildCache(String regionName, boolean autoCreate) throws CacheException {
        ShardedRedisCache cache = redisMap.get(regionName);
        if (Objects.isNull(cache)) {
            cache = new ShardedRedisCache(regionName, pool);
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


        int i = 1;
        String url;
        String password;
        List<JedisShardInfo> infos = new ArrayList<>();
        while (true) {
            url = config.getString("redis.shared.url." + i, null);
            if (StringUtil.isBlank(url)) {
                break;
            }
            JedisShardInfo info = new JedisShardInfo(url, config.getInt("redis.shared.port." + i, 6379));
            password = config.getString("redis.shared.password." + i, null);
            if (!StringUtil.isBlank(password)) {
                info.setPassword(password);
            }
            infos.add(info);
            i++;
        }


        if (CollectionUtil.isEmpty(infos)) {
            throw new CacheException("create sharded redis pool failure,no sharded url");
        } else {
            pool = new ShardedJedisPool(poolConfig, infos, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
        }
    }

    @Override
    public void stop() {
        pool.destroy();
        redisMap.clear();
    }
}
