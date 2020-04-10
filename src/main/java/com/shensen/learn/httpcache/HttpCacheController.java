package com.shensen.learn.httpcache;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpCacheController {

    private final DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    @GetMapping("/cache")
    public ResponseEntity<String> cache(
            // 浏览器验证文档内容是否修改时传入的Last-Modified
            @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince)
            throws Exception {

        // 文档最后修改时间（去掉毫秒值）
        long lastModifiedMillis = getLastModified() / 1000 * 1000;
        // 当前系统时间（去掉毫秒值）
        long now = System.currentTimeMillis() / 1000 * 1000;
        // 文档可以在游览器端/proxy上缓存多久（单位：秒）
        long maxAge = 20;

        // 判断内容是否修改了，此处使用等值判断
        if (StringUtils.isNotBlank(ifModifiedSince)) {
            Date ifModifiedsince = dateFormat.parse(ifModifiedSince);
            if (ifModifiedsince.getTime() == lastModifiedMillis) {
                MultiValueMap<String, String> headers = new HttpHeaders();
                // 当前时间
                headers.add("Date", dateFormat.format(new Date(now)));
                // 过期时间，http1.0支持
                headers.add("Expires", dateFormat.format(new Date(now + maxAge * 1000)));
                // 文档生存时间,http1.1支持
                headers.add("Cache-Control", "max-age=" + maxAge);
                return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
            }
        }

        String body = "<a href=''>点我访问链接</a>";
        MultiValueMap<String, String> headers = new HttpHeaders();
        // 当前时间
        headers.add("Date", dateFormat.format(new Date(now)));
        // 文档修改时间
        headers.add("Last-Modified", dateFormat.format(new Date(lastModifiedMillis)));
        // 过期时间，http1.0支持
        headers.add("Expires", dateFormat.format(new Date(now + maxAge * 1000)));
        // 文档生存时间,http1.1支持
        headers.add("Cache-Control", "max-age=" + maxAge);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/cache/etag")
    public ResponseEntity<String> cacheEtag(
            //浏览器验证文档内容的实体 If-None-Match
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch)
            throws Exception {

        //当前系统时间
        long now = System.currentTimeMillis();
        //文档可以在浏览器端/proxy上缓存多久
        long maxAge = 10;

        String body = "<a href=''>点我访问当前链接</a>";

        //弱实体
        String etag = "W/\"" + md5Hex(body) + "\"";

        if (StringUtils.equals(ifNoneMatch, etag)) {
            return new ResponseEntity<String>(HttpStatus.NOT_MODIFIED);
        }

        MultiValueMap<String, String> headers = new HttpHeaders();
        //ETag http 1.1支持
        headers.add("ETag", etag);
        //当前系统时间
        headers.add("Date", dateFormat.format(new Date(now)));
        //文档生存时间 http 1.1支持
        headers.add("Cache-Control", "max-age=" + maxAge);
        return new ResponseEntity<String>(body, headers, HttpStatus.OK);
    }

    @GetMapping("/httpclient/cache")
    public ResponseEntity<String> httpclientCache() throws IOException {
        // EhCache缓存存储
        CacheManager cacheManager = CacheManager.create();
        net.sf.ehcache.Cache httpCache = cacheManager.getCache("httpCache");
        EhcacheHttpCacheStorage cachestorage = new EhcacheHttpCacheStorage(
                httpCache);

        // 创建 Httpclient
        CloseableHttpClient httpClient = CachingHttpClients.custom()
                .setCacheConfig(cacheconfig) // 缓存配置
                .setHttpCacheStorage(cachestorage) //缓存存储
                .build();

        HttpGet get = new HttpGet("https://item.jd.com/56656942584.html");
        get.addHeader("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");

        HttpCacheContext context = HttpCacheContext.create();
        CloseableHttpResponse response = httpClient.execute(get, context);
        String body = "";
        //try {
            CacheResponseStatus responseStatus = context.getCacheResponseStatus();
            switch (responseStatus) {
                case CACHE_HIT:
                    body = "HIT：响应命中，返回缓存的响应内容，不会发送请求到上游服务器";
                    break;
                case CACHE_MODULE_RESPONSE:
                    body = "MODULE_RESPONSE：缓存直接生成响应";
                    break;
                case CACHE_MISS:
                    body = "MISS：缓存未命中，响应来自上游服务器";
                    break;
                case VALIDATED:
                    body = "VALIDATED：缓存不新鲜需要重新到上游服务器验证，且验证后返回缓存中响应";
                    break;
            }
        /*} catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.close();
        }*/

        return new ResponseEntity<String>(body, HttpStatus.OK);
    }

    // 缓存配置
    CacheConfig cacheconfig = CacheConfig.custom()
            .setMaxCacheEntries(1000) //最多缓存1000个条目
            .setMaxObjectSize(1 * 1024 * 1024) // 缓存对象最大为1MB
            .setAsynchronousWorkersCore(5) //异步更新缓存线程池最小空闲线程数
            .setAsynchronousWorkersMax(10) //异步更新缓存线程池最大线程数
            .setRevalidationQueueSize(10000) //异步更新线程池队列大小
            .build();

    // BasicHttp 缓存存储，底层LinkedHashMap实现，缓存基于LRU算法，没有时间过期策略
    BasicHttpCacheStorage cachestorage = new BasicHttpCacheStorage(cacheconfig);

    Cache<String, Long> lastModifiedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    private long getLastModified() throws ExecutionException {
        return lastModifiedCache.get("lastModifiedSince", () -> System.currentTimeMillis());
    }
}
