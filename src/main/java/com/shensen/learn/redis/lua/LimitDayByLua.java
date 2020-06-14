package com.shensen.learn.redis.lua;

import static com.shensen.learn.redis.JedisManager.getJedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;

/**
 * 按天限流脚本
 *
 * @author Alwyn
 * @date 2020-06-08 11:40
 */
public class LimitDayByLua {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Limit(getJedis()));
        }
    }

    public static final String LIMIT_LUA;

    static {
        StringBuilder limitLua = new StringBuilder();
        limitLua.append("local key = KEYS[1]\n");
        limitLua.append("local limitTime = tonumber(ARGV[1])\n");
        limitLua.append("local currentTime = tonumber(redis.call('get', key) or '0')\n");
        limitLua.append("if currentTime + 1 > limitTime then\n");
        limitLua.append("   return 0\n");
        limitLua.append("else\n");
        limitLua.append("   redis.call('incrby', key, '1')\n");
        limitLua.append("end\n");
        limitLua.append("return redis.call('pexpireAt', key, tonumber(ARGV[2]))\n");
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
                argv.add("1"); // 失效时间前访问次数
                // 失效时间时刻毫秒数
                argv.add(String.valueOf(System.currentTimeMillis() + 15 * 1000));
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
