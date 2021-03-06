import com.alisoft.nano.bench.Nano;
import com.star.cache.Cache;
import com.star.cache.CacheProvider;
import com.star.cache.ehcache.EhCacheProvider;
import net.sf.ehcache.Ehcache;
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

    @Before
    public void startUp() {
        provider = new EhCacheProvider();
        provider.start();
    }

    @After
    public void tearDown() {
        provider.stop();
    }

    @Test
    public void testEhcache() throws URISyntaxException, InterruptedException {
        Cache cache = provider.buildCache("test", true);
        cache.put("1", "hello", 0);
        cache.put("2", "world", 0);

//        cache.evict("1", "2");

        System.out.println(cache.exists("3"));


    }
}
