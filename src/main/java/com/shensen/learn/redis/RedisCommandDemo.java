package com.shensen.learn.redis;

import static com.shensen.learn.redis.JedisManager.getJedis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.shensen.learn.dto.LotteryActivity;
import com.shensen.learn.dto.LotteryAwards;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;

/**
 * Redis命令实战
 *
 * @author Alwyn
 * @date 2020-06-08 11:41
 */
public class RedisCommandDemo {

    public static final String key = "Lottery:12323";

    public static void main(String[] args) {
        Jedis jedis = getJedis();

        LotteryAwards lotteryA = new LotteryAwards();
        lotteryA.setAwardsId(1L);
        lotteryA.setAwardsStatus(1);
        lotteryA.setAwardCount(20);
        lotteryA.setCommodityCode("000000013360153061");
        lotteryA.setWinningCount(5);
        lotteryA.setAwardsList(Arrays.asList(1L, 2L, 3L));

        hmset(jedis, "Lottery:12323:1", lotteryA, 60 * 60);
        System.out.println(jedis.hgetAll("Lottery:12323:1"));
        System.out.println(JSONArray.parseArray(jedis.hget("Lottery:12323:2", "awardsList"), Long.class));
        jedis.close();
    }

    private static void initLotteryActivity(Jedis jedis) {
        LotteryActivity activity = new LotteryActivity();
        activity.setLotteryAwardsList(Arrays.asList(1L, 2L, 3L));
        hmset(jedis, key, activity, 60 * 60);

        LotteryAwards lotteryA = new LotteryAwards();
        lotteryA.setAwardsId(1L);
        lotteryA.setAwardsStatus(1);
        lotteryA.setAwardCount(20);
        lotteryA.setCommodityCode("000000013360153061");
        lotteryA.setWinningCount(5);
        lotteryA.setAwardsList(Arrays.asList(1L, 2L, 3L));

        hmset(jedis, "Lottery:12323:1", lotteryA, 60 * 60);

        LotteryAwards lotteryB = new LotteryAwards();
        lotteryB.setAwardsId(2L);
        lotteryB.setAwardsStatus(1);
        lotteryB.setAwardCount(5);
        lotteryB.setCommodityCode("000000018360153062");
        lotteryB.setWinningCount(1);
        hmset(jedis, "Lottery:12323:2", lotteryB, 60 * 60);

        LotteryAwards lotteryC = new LotteryAwards();
        lotteryC.setAwardsId(3L);
        lotteryC.setAwardsStatus(1);
        lotteryC.setAwardCount(14);
        lotteryC.setCommodityCode("000000019360153063");
        lotteryC.setWinningCount(4);
        hmset(jedis, "Lottery:12323:3", lotteryC, 60 * 60);
        System.out.println(Arrays.asList(lotteryA, lotteryB, lotteryC));
    }

    private static <T> void hmset(Jedis jedis, final String key, T object, final int seconds) {
        Map<String, String> hash = JSON
                .parseObject(JSON.toJSONString(object), new TypeReference<Map<String, String>>() {
                });
        jedis.hmset(key, hash);
        jedis.expire(key, seconds);
    }

    private static void hmset(Jedis jedis) {
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

        jedis.hincrBy(key, "winningCount", 1);
        System.out.println(jedis
                .hmget(key, new String[]{"winningCount"}));

        LotteryAwards lotteryB = new LotteryAwards();
        lotteryB.setAwardsStatus(2);
        hash = JSON.parseObject(JSON.toJSONString(lotteryB), new TypeReference<Map<String, String>>() {
        });
        System.out.println(hash);
        System.out.println(jedis.hmset(key, hash));
        System.out.println(jedis
                .hmget(key, new String[]{"winningCount", "awardsStatus"}));
    }

    private static void ttl(Jedis jedis) {
        System.out.println(jedis.get("limit:time:100022:1001"));
        System.out.println(jedis.pexpireAt("limit:time:100022:1001", 1589907087000L));
        System.out.println(jedis.ttl("limit:time:100022:1001"));
    }

    private static void hmget(Jedis jedis) {
        LotteryAwards lottery = new LotteryAwards();
        lottery.setAwardsId(1L);
        lottery.setAwardsStatus(1);
        lottery.setAwardCount(20);
        lottery.setCommodityCode("112121212");
        lottery.setWinningCount(5);

        Map<String, String> hash = JSON
                .parseObject(JSON.toJSONString(lottery), new TypeReference<Map<String, String>>() {
                });
        System.out.println(jedis.hmset(key, hash));
        List<String> list = jedis
                .hmget(key, new String[]{"antipatePersonTime", "winningCount", "aa"});
        System.out.println(list);

        jedis.hincrBy(key, "winningCount", 5);
        list = jedis
                .hmget(key, new String[]{"winningCount"});
        System.out.println(list);

        jedis.expire(key, 10);
    }
}
