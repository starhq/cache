package com.star.cache.redis.shard;

import com.star.cache.Cache;
import com.star.cache.exception.CacheException;
import com.star.collection.ArrayUtil;
import com.star.config.Config;
import com.star.io.CharsetUtil;
import com.star.io.serializer.SerializationUtils;
import com.star.string.StringUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 分片的版本
 * <p>
 * Created by starhq on 2016/12/1.
 */
public class ShardedRedisCache implements Cache {
    /**
     * hash中的key
     */
    private final byte[] regionBytes;
    /**
     * redis连接池
     */
    private final ShardedJedisPool pool;

    /**
     * 构造方法
     *
     * @param region key
     * @param pool   连接池
     */
    public ShardedRedisCache(final String region, final ShardedJedisPool pool) {
        String reg = null;
        if (StringUtil.isBlank(region)) {
            reg = StringUtil.UNDERLINE;
        } else {
            reg = region;
        }
        reg = getRegionName(reg);
        this.pool = pool;
        this.regionBytes = reg.getBytes();
    }


    /**
     * 给key增加个包名
     *
     * @param region key
     * @return 分装好的key
     */
    private String getRegionName(final String region) {
        final Config config = new Config("config.properties");
        final String namespace = config.getString("redis.namespace", "");
        return StringUtil.isBlank(namespace) ? region : namespace + ":" + region;
    }

    /**
     * 将key转成byte数组
     *
     * @param key
     * @return key的字节数组
     */
    private byte[] getKeyName(final Serializable key) {
        final StringBuilder builder = new StringBuilder();
        if (key instanceof Number) {
            builder.append("N:");
        } else if (key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer) {
            builder.append("S:");
        } else {
            builder.append("O:");
        }
        builder.append(key);
        return builder.toString().getBytes();
    }

    /**
     * 获得缓存内容
     *
     * @param key 键
     * @return value
     * @throws CacheException 缓存异常
     */
    @Override
    public Object get(final Serializable key) throws CacheException {
        Object result = null;
        if (!Objects.isNull(key)) {
            try (ShardedJedis cache = pool.getResource()) {
                final Config config = new Config("config.properties");
                final byte[] bytes = config.getBool("redis.hash", true) ? cache.hget(regionBytes, getKeyName(key))
                        : cache.get(getKeyName(key));
                if (!Objects.isNull(bytes) && bytes.length > 0) {
                    result = SerializationUtils.productSerializer(config.getString("serialization", "Java"))
                            .deserialize(bytes);
                }
            } catch (Exception e) {
                if (e instanceof IOException || e instanceof NullPointerException) {
                    evict(key);
                }
                throw new CacheException(StringUtil.format("get cache from redis failure: {}", e.getMessage()), e);
            }
        }
        return result;
    }

    /**
     * 缓存数据 hash实现的话有效期没有作用
     * <p>
     *
     * @param key     键
     * @param value   值
     * @param seconds 超时时间
     * @throws CacheException 缓存异常
     */
    @Override
    public void put(final Serializable key, final Object value, final int seconds) throws CacheException {
        if (Objects.isNull(key)) {
            return;
        } else if (Objects.isNull(value)) {
            evict(key);
        } else {
            try (ShardedJedis cache = pool.getResource()) {
                final Config config = new Config("config.properties");
                if (config.getBool("redis.hash", true)) {
                    cache.hset(regionBytes, getKeyName(key), SerializationUtils.productSerializer(config.getString
                            ("serialization", "Java")).serialize(value));
                } else {

                    if (seconds > 0) {
                        cache.setex(getKeyName(key), seconds, SerializationUtils.productSerializer(config.getString
                                ("serialization", "Java")).serialize(value));
                    } else {
                        cache.set(getKeyName(key), SerializationUtils.productSerializer(config.getString
                                ("serialization", "Java")).serialize(value));
                    }
                }

            } catch (Exception e) {
                throw new CacheException(StringUtil.format("put cache in redis failure: {}", e.getMessage()), e);
            }
        }
    }

    /**
     * 更新缓存
     *
     * @param key     键
     * @param value   值
     * @param seconds 超时时间
     * @throws CacheException 缓存异常
     */
    @Override
    public void update(final Serializable key, final Object value, final int seconds) throws CacheException {
        put(key, value, seconds);
    }

    /**
     * 失效缓存
     *
     * @param key 键
     * @throws CacheException 缓存异常
     */
    @Override
    public void evict(final Serializable key) throws CacheException {
        if (!Objects.isNull(key)) {
            try (ShardedJedis cache = pool.getResource()) {
                final Config config = new Config("config.properties");
                if (config.getBool("redis.hash", true)) {
                    cache.hdel(regionBytes, getKeyName(key));
                } else {
                    cache.del(getKeyName(key));
                }
            } catch (Exception e) {
                throw new CacheException(StringUtil.format("evict cache from redis failure: {}", e.getMessage()), e);
            }
        }

    }

    /**
     * 批量失效缓存
     * <p>
     * 非hash的时候慎用
     *
     * @param keys 键的集合
     * @throws CacheException 缓存异常
     */
    @Override
    public void evict(final Serializable... keys) throws CacheException {
        if (!ArrayUtil.isEmpty(keys)) {
            try (ShardedJedis cache = pool.getResource()) {
                final int length = keys.length;
                byte[][] bytes = new byte[length][];
                for (int i = 0; i < length; i++) {
                    bytes[i] = getKeyName(keys[i]);
                }
                final Config config = new Config("config.properties");
                if (config.getBool("redis.hash", true)) {
                    cache.hdel(regionBytes, bytes);
                } else {
                    for (Jedis jedis : cache.getAllShards()) {
                        jedis.del(bytes);
                    }
                }

            } catch (Exception e) {
                throw new CacheException(StringUtil.format("batch evict cache from redis failure: {}", e.getMessage()),
                        e);
            }
        }
    }

    /**
     * 清空缓存
     *
     * @throws CacheException 缓存异常
     */
    @Override
    public void clear() throws CacheException {
        try (ShardedJedis cache = pool.getResource()) {
            final Config config = new Config("config.properties");
            if (config.getBool("redis.hash", true)) {
                cache.del(regionBytes);
            } else {
                for (Jedis jedis : cache.getAllShards()) {
                    jedis.flushAll();
                }

            }

        } catch (Exception e) {
            throw new CacheException(StringUtil.format("clear cache in redis failure: {}", e.getMessage()), e);
        }
    }

    /**
     * 销毁缓存
     *
     * @throws CacheException 缓存异常
     */
    @Override
    public void destory() throws CacheException {
        clear();
    }

    /**
     * 获得键集合
     *
     * @return 键的集合
     * @throws CacheException 缓存异常
     */
    @Override
    public List<?> getKeys() throws CacheException {
        try (ShardedJedis cache = pool.getResource()) {
            final Config config = new Config("config.properties");
            List<String> keys = new ArrayList<>();
            if (config.getBool("redis.hash", true)) {
                for (byte[] bytes : cache.hkeys(regionBytes)) {
                    keys.add(StringUtil.byte2String(bytes, CharsetUtil.DEFAULT));
                }
            } else {
                for (Jedis jedis : cache.getAllShards()) {
                    for (String strings : jedis.keys("*")) {
                        keys.add(strings);
                    }
                }
            }
            return keys;
        } catch (
                Exception e)

        {
            throw new CacheException(StringUtil.format("get keys from redis failure: {}", e.getMessage()), e);
        }

    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return
     * @throws CacheException 缓存异常
     */
    @Override
    public boolean exists(final Serializable key) throws CacheException {
        try (ShardedJedis cache = pool.getResource()) {
            final Config config = new Config("config.properties");
            return config.getBool("redis.hash", true) ? cache.hexists(regionBytes, getKeyName(key)) : cache.exists
                    (getKeyName(key));
        } catch (Exception e) {
            throw new CacheException(StringUtil.format("detect weather key in cache from redis failure: {}", e
                    .getMessage()), e);
        }
    }
}
