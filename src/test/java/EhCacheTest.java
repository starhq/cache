import com.alisoft.nano.bench.Nano;
import com.star.cache.Cache;
import com.star.cache.CacheProvider;
import com.star.cache.ehcache.EhCacheProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

/**
 * Created by starhq on 2016/11/24.
 */
public class EhCacheTest {

    protected static int measurements = 100; // 测量次数
    protected static int threads = 100; // 线程数
    protected static int SerialTimes = 100; // 每个线程执行序列化次数

    private EhCacheProvider provider;
    private Cache cache;

    @Before
    public void startUp() {
        provider = new EhCacheProvider();
        provider.start();
        cache = provider.buildCache("test", true);
    }

    @After
    public void tearDown() {
        provider.stop();
    }

    @Test
    public void testEhcache() throws URISyntaxException, InterruptedException {

        Nano.bench().measurements(measurements).threads(threads).measure("ehcache测试", new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < SerialTimes; i++) {

                    cache.put("123", "hellworld", 0);
                    cache.evict("123");
                }
            }
        });


    }
}
