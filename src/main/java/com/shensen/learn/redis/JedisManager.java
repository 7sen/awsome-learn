package com.shensen.learn.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * JedisManager
 *
 * @author Alwyn
 * @date 2020-06-08 11:39
 */
public class JedisManager {

    private static JedisPool jedisPool;

    static {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setMinIdle(0);
        jedisPool = new JedisPool(config, "10.243.136.182", 6379);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
