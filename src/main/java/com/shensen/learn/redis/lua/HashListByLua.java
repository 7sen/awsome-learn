package com.shensen.learn.redis.lua;

import static com.shensen.learn.redis.JedisManager.getJedis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import redis.clients.jedis.Jedis;

/**
 * Hash库存扣减脚本
 *
 * @author Alwyn
 * @date 2020-06-08 11:42
 */
public class HashListByLua {

    public static void main(String[] args) {
        Jedis jedis = getJedis();

        List<String> keys = Arrays.asList("Lottery:12323:1", "Lottery:12323:2", "Lottery:12323:3");
        List<String> argv = new ArrayList<>();
        String scriptLoad = jedis.scriptLoad(LUA_SCRIPT);
        Object result = jedis.evalsha(scriptLoad, keys, argv);
        System.out.println(JSON.parseObject(JSON.toJSONString(result), new TypeReference<List<String>>() {
        }));

        System.out.println(jedis.hgetAll("Lottery:12323:1"));
        jedis.close();
    }

    public static final String LUA_SCRIPT;

    static {
        /**
         * 根据keys获取多个hash值
         */
        StringBuilder luaScript = new StringBuilder();
        luaScript.append("local result ={}\n");
        luaScript.append("local temp\n");
        luaScript.append("for i = 1,#(KEYS) do\n");
        luaScript.append("  temp = redis.call('hgetall', KEYS[i]) \n");
        luaScript.append("  result[i] = table.concat(temp, ' ')");
        luaScript.append("end\n");
        luaScript.append("return result\n");
        LUA_SCRIPT = luaScript.toString();
    }

}
