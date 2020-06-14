package com.shensen.learn.redis.lua;

import static com.shensen.learn.redis.JedisManager.getJedis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shensen.learn.dto.LotteryAwards;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import redis.clients.jedis.Jedis;

/**
 * Hash库存扣减脚本
 *
 * @author Alwyn
 * @date 2020-06-08 11:42
 */
public class StockHashByLua {

    public static final String key = "Lottery:12323:121212";

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        //initLotteryAwards(jedis);

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 2; i++) {
            executorService.execute(new StockHincrBy(jedis));
        }
    }

    private static void initLotteryAwards(Jedis jedis) {
        LotteryAwards lotteryA = new LotteryAwards();
        lotteryA.setAwardsId(1L);
        lotteryA.setAwardsStatus(1);
        lotteryA.setAwardCount(20);
        lotteryA.setCommodityCode("112121212");
        lotteryA.setWinningCount(5);
        Map<String, String> hash = JSON
                .parseObject(JSON.toJSONString(lotteryA), new TypeReference<Map<String, String>>() {
                });
        System.out.println(jedis.hmset(key, hash));
    }

    public static final String LUA_SCRIPT;

    static {
        /**
         *
         * 扣减库存Lua脚本
         * 库存（stock）0：表示没有剩余数量
         * 库存（stock）大于0：表示剩余库存
         *
         * @return
         *      >0:剩余数量
         *      -1:数量不足扣减
         *      -2:数量未初始化
         */
        StringBuilder luaScript = new StringBuilder();
        luaScript.append("local key = KEYS[1]");
        luaScript.append("if (redis.call('hexists', key, ARGV[1]) == 1) then\n");
        luaScript.append("    local count = tonumber(redis.call('hget', key, ARGV[1]))\n");
        luaScript.append("    local num = tonumber(ARGV[2])\n");
        luaScript.append("    if (count >= num) then\n");
        luaScript.append("        return redis.call('hincrBy', key, ARGV[1], 0 - num)\n");
        luaScript.append("    end\n");
        luaScript.append("    return -1\n");
        luaScript.append("end\n");
        luaScript.append("return -2");
        LUA_SCRIPT = luaScript.toString();
    }

    static class StockHincrBy implements Runnable {

        private Jedis jedis;

        public StockHincrBy(Jedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public void run() {
            try {

                List<String> keys = new ArrayList<>();
                keys.add(key); // 活动奖项缓存key
                List<String> argv = new ArrayList<>();
                argv.add("awardCount");
                argv.add("1");
                String scriptLoad = jedis.scriptLoad(LUA_SCRIPT);
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
