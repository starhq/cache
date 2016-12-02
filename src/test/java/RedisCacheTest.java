import com.alisoft.nano.bench.Nano;
import com.star.cache.Cache;
import com.star.cache.ehcache.EhCacheProvider;
import com.star.cache.redis.single.RedisCacheProvider;
import com.star.config.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URISyntaxException;

/**
 * Created by starhq on 2016/11/24.
 */
public class RedisCacheTest {

    protected static int measurements = 100; // 测量次数
    protected static int threads = 100; // 线程数
    protected static int SerialTimes = 100; // 每个线程执行序列化次数

    private RedisCacheProvider provider;

    @Before
    public void startUp() {
        provider = new RedisCacheProvider();
        provider.start();
    }

    @After
    public void tearDown() {
        provider.stop();
    }

    @Test
    public void testRedis() throws Exception {
        Cache cache = provider.buildCache("test", true);
        cache.put("1", "hello", 0);
        cache.put("2", "world", 0);
        cache.put("3", "starhq", 0);
        cache.clear();
//        System.out.println(cache.getKeys());
//        System.out.println(cache.exists("1"));
//        cache.evict("1");
//        System.out.println(cache.exists("1"));
//        cache.evict("2", "3");
//        System.out.println(cache.getKeys());

    }
}
