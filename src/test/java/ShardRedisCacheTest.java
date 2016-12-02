import com.star.cache.Cache;
import com.star.cache.redis.shard.ShardedRedisCacheProvider;
import com.star.cache.redis.single.RedisCacheProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by starhq on 2016/11/24.
 */
public class ShardRedisCacheTest {

    protected static int measurements = 100; // 测量次数
    protected static int threads = 100; // 线程数
    protected static int SerialTimes = 100; // 每个线程执行序列化次数

    private ShardedRedisCacheProvider provider;

    @Before
    public void startUp() {
        provider = new ShardedRedisCacheProvider();
        provider.start();
    }

    @After
    public void tearDown() {
        provider.stop();
    }

    @Test
    public void testRedis() throws Exception {
        Cache cache = provider.buildCache("test", true);


        cache.clear();

    }

}
