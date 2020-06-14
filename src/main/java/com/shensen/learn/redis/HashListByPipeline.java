package com.shensen.learn.redis;

import static com.shensen.learn.redis.JedisManager.getJedis;

import com.alibaba.fastjson.JSON;
import com.shensen.learn.dto.LotteryAwards;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Hash批量读取使用Pipeline.
 *
 * @author Alwyn
 * @date 2020-06-08 11:42
 */
public class HashListByPipeline {

    public static void main(String[] args) {
        Jedis jedis = getJedis();

        List<String> keys = Arrays.asList("Lottery:12323:1", "Lottery:12323:2", "Lottery:12323:3");
        List<LotteryAwards> lotteryAwardsList = new ArrayList<>();
        Pipeline pipeline = jedis.pipelined();
        for (String key : keys) {
            LotteryAwards lotteryAwardsA = JSON.parseObject(JSON.toJSONString(jedis.hgetAll(key)), LotteryAwards.class);
            lotteryAwardsList.add(lotteryAwardsA);
        }
        pipeline.sync();
        System.out.println(lotteryAwardsList);
        jedis.close();
    }

}
