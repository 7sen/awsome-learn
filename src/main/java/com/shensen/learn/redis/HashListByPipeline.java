package com.shensen.learn.redis;

import static com.shensen.learn.redis.JedisManager.getJedis;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.shensen.learn.dto.LotteryAwards;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

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
        Map<String, Response<Map<String, String>>> responseMap = new HashMap<>(keys.size());
        for (String key : keys) {
            responseMap.put(key, pipeline.hgetAll(key));
        }
        pipeline.sync();
        LotteryAwards awards = new LotteryAwards();
        awards.setAwardsList(Arrays.asList(5L, 3L, 14L));
        Map<String, String> hash = JSON
                .parseObject(JSON.toJSONString(awards), new TypeReference<Map<String, String>>() {
                });
        pipeline.hmset("Lottery:12323:2", hash);
        pipeline.sync();
        for (Iterator<Response<Map<String, String>>> iterator = responseMap.values().iterator(); iterator.hasNext(); ) {
            Map<String, String> next = iterator.next().get();
            if (MapUtil.isNotEmpty(next)) {
                lotteryAwardsList.add(JSONObject.parseObject(JSON.toJSONString(next), LotteryAwards.class));
            }
        }
        System.out.println(lotteryAwardsList);
        jedis.close();
    }

}
