package com.shensen.learn.redis.lua;

import static com.shensen.learn.redis.JedisManager.getJedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import redis.clients.jedis.Jedis;

/**
 * 库存扣将脚本
 *
 * @author Alwyn
 * @date 2020-06-08 11:41
 */
public class StockByLua {

    public static void main(String[] args) {
        initStock(3, 10 * 60);

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(new StockIncr(getJedis()));

        }
    }

    private static void initStock(int stack, int seconds) {
        Jedis jedis = getJedis();
        jedis.setex("Lottery:12323:10001", seconds, String.valueOf(stack));
        jedis.close();
    }

    public static final String STOCK_LUA;

    static {
        /**
         *
         * 扣减库存Lua脚本
         * 库存（stock）0：表示没有库存
         * 库存（stock）大于0：表示剩余库存
         *
         * @return
         *      >0:剩余库存（扣减之后剩余的库存）
         *      -1:库存不足
         *      -2:库存未初始化
         */
        StringBuilder stockLua = new StringBuilder();
        stockLua.append("local key = KEYS[1]");
        stockLua.append("if (redis.call('exists', key) == 1) then\n");
        stockLua.append("    local stock = tonumber(redis.call('get', key))\n");
        stockLua.append("    local num = tonumber(ARGV[1])\n");
        stockLua.append("    if (stock >= num) then\n");
        stockLua.append("        return redis.call('incrby', key, 0 - num)\n");
        stockLua.append("    end\n");
        stockLua.append("    return -1\n");
        stockLua.append("end\n");
        stockLua.append("return -2");
        STOCK_LUA = stockLua.toString();
        System.out.println("库存扣减脚本:\n" + STOCK_LUA);
    }

    static class StockIncr implements Runnable {

        private Jedis jedis;

        public StockIncr(Jedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public void run() {
            try {

                List<String> keys = new ArrayList<>();
                keys.add("Lottery:12323:10001"); // 活动奖项缓存key
                List<String> argv = new ArrayList<>();
                argv.add("1"); // 减库存数
                // 失效时间时刻毫秒数
                argv.add(String.valueOf(System.currentTimeMillis() + 2 * 60 * 1000));
                String scriptLoad = jedis.scriptLoad(STOCK_LUA);
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
