package com.shensen.learn.redis.lua;

import static com.shensen.learn.redis.JedisManager.getJedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;

/**
 * 按秒限流脚本
 *
 * @author Alwyn
 * @date 2020-06-08 11:40
 */
public class LimitSecondByLua {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Limit(getJedis()));
        }
    }

    public static final String LIMIT_LUA;

    static {
        StringBuilder limitLua = new StringBuilder();
        limitLua.append("local key = KEYS[1]\n");
        limitLua.append("local seconds = tonumber(ARGV[1])\n");
        limitLua.append("local limitTime = tonumber(ARGV[2])\n");
        limitLua.append("local currentTime = tonumber(redis.call('incrby', key, '1'))\n");
        limitLua.append("if currentTime == 1 then\n");
        limitLua.append("   return redis.call('expire', key, seconds)\n");
        limitLua.append("elseif currentTime > limitTime then\n");
        limitLua.append("   return 0\n");
        limitLua.append("else\n");
        limitLua.append("   return 1\n");
        limitLua.append("end\n");
        LIMIT_LUA = limitLua.toString();
        System.out.println("限流LUA脚本:\n" + LIMIT_LUA);
    }

    static class Limit implements Runnable {

        private Jedis jedis;

        public Limit(Jedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public void run() {
            try {

                List<String> keys = new ArrayList<>();
                keys.add("limit:time:100022:1001"); // 限流针对的key
                List<String> argv = new ArrayList<>();
                argv.add("1");// 1秒钟，允许最大访问量5个，若超过则限流
                argv.add("5");
                String scriptLoad = jedis.scriptLoad(LIMIT_LUA);
                Object result = jedis.evalsha(scriptLoad, keys, argv);
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                jedis.close();
            }
        }
    }

}
