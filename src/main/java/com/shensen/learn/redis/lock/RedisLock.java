package com.shensen.learn.redis.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;

/**
 * Redis分布式锁
 * 使用 SET key value [EX seconds] NX 实现
 * <p>
 * Redis 官方 SET 命令详细介绍参见：
 * http://doc.redisfans.com/string/set.html
 * <p>
 * 命令 SET key value [EX seconds] [PX milliseconds] [NX|XX]，其中：<br>
 * EX second ：设置键的过期时间为 second 秒。SET key value EX second 效果等同于 SETEX key second value 。<br>
 * PX millisecond ：设置键的过期时间为 millisecond 毫秒。SET key value PX millisecond 效果等同于 PSETEX key millisecond value 。<br>
 * NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value 。
 * XX ：只在键已经存在时，才对键进行设置操作
 * <p>
 * 命令 SET key value [EX seconds] NX 是一种在 Redis 中实现锁的简单方法
 * <p>
 * 客户端执行以上的命令：
 * <p>
 * 如果服务器返回 OK ，那么这个客户端获得锁。
 * 如果服务器返回 NIL ，那么客户端获取锁失败，可以在稍后再重试。
 *
 * @author Alwyn
 * @date 2020-05-21 17:01
 */
public class RedisLock {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);

    private Jedis jedis;

    /**
     * 将key 的值设为value ，当且仅当key 不存在，等效于 SETNX
     */
    public static final String NX = "NX";

    /**
     * seconds — 以秒为单位设置 key 的过期时间，等效于EXPIRE key seconds
     */
    public static final String EX = "EX";

    /**
     * 调用set后的返回值
     */
    public static final String OK = "OK";

    /**
     * 默认请求锁的超时时间(ms 毫秒)
     */
    private static final long TIME_OUT = 100;

    /**
     * 默认锁的有效时间(s)
     */
    public static final int EXPIRE = 60;

    /**
     * 解锁的lua脚本
     */
    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call('get', KEYS[1]) == ARGV[1]\n");
        sb.append("then ");
        sb.append("    return redis.call('del', KEYS[1])\n");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    /**
     * 锁标志对应的key
     */
    private String lockKey;

    /**
     * 记录到日志的锁标志对应的key
     */
    private String lockKeyLog = "";

    /**
     * 锁对应的值
     */
    private String lockValue;

    /**
     * 锁的有效时间(s)
     */
    private int expireTime = EXPIRE;

    /**
     * 请求锁的超时时间(ms)
     */
    private long timeOut = TIME_OUT;

    /**
     * 锁标记
     */
    private volatile boolean locked = false;

    final Random random = new Random();

    /**
     * 使用默认的锁过期时间和请求锁的超时时间
     *
     * @param jedis
     * @param lockKey 锁的key（Redis的Key）
     */
    public RedisLock(Jedis jedis, String lockKey) {
        this.jedis = jedis;
        this.lockKey = lockKey + "_lock";
    }

    /**
     * 使用默认的请求锁的超时时间，指定锁的过期时间
     *
     * @param jedis
     * @param lockKey 锁的key（Redis的Key）
     * @param expireTime 锁的过期时间(单位：秒)
     */
    public RedisLock(Jedis jedis, String lockKey, int expireTime) {
        this(jedis, lockKey);
        this.expireTime = expireTime;
    }

    /**
     * 使用默认的锁的过期时间，指定请求锁的超时时间
     *
     * @param jedis
     * @param lockKey 锁的key（Redis的Key）
     * @param timeOut 请求锁的超时时间(单位：毫秒)
     */
    public RedisLock(Jedis jedis, String lockKey, long timeOut) {
        this(jedis, lockKey);
        this.timeOut = timeOut;
    }

    /**
     * 锁的过期时间和请求锁的超时时间都是用指定的值
     *
     * @param jedis
     * @param lockKey 锁的key（Redis的Key）
     * @param expireTime 锁的过期时间(单位：秒)
     * @param timeOut 请求锁的超时时间(单位：毫秒)
     */
    public RedisLock(Jedis jedis, String lockKey, int expireTime, long timeOut) {
        this(jedis, lockKey, expireTime);
        this.timeOut = timeOut;
    }

    /**
     * 尝试获取锁 超时返回
     *
     * @return
     */
    public boolean tryLock() {
        // 生成随机key
        lockValue = UUID.randomUUID().toString();
        // 请求锁超时时间，纳秒
        long timeout = timeOut * 1000000;
        // 系统当前时间，纳秒
        long nowTime = System.nanoTime();
        while ((System.nanoTime() - nowTime) < timeout) {
            if (OK.equalsIgnoreCase(this.set(lockKey, lockValue, expireTime))) {
                locked = true;
                // 上锁成功结束请求
                return locked;
            }

            // 每次请求等待一段时间
            seleep(10, 50000);
        }
        return locked;
    }

    /**
     * 尝试获取锁 立即返回
     *
     * @return 是否成功获得锁
     */
    public boolean lock() {
        lockValue = UUID.randomUUID().toString();
        // 不存在则添加 且设置过期时间（单位ms）
        String result = set(lockKey, lockValue, expireTime);
        locked = OK.equalsIgnoreCase(result);
        return locked;
    }

    /**
     * 以阻塞方式的获取锁
     *
     * @return 是否成功获得锁
     */
    public boolean lockBlock() {
        lockValue = UUID.randomUUID().toString();
        while (true) {
            //不存在则添加 且设置过期时间（单位ms）
            String result = set(lockKey, lockValue, expireTime);
            if (OK.equalsIgnoreCase(result)) {
                locked = true;
                return locked;
            }

            // 每次请求等待一段时间
            seleep(10, 50000);
        }
    }

    /**
     * 解锁
     * <p>
     * 可以通过以下修改，让这个锁实现更健壮：
     * <p>
     * 不使用固定的字符串作为键的值，而是设置一个不可猜测（non-guessable）的长随机字符串，作为口令串（token）。
     * 不使用 DEL 命令来释放锁，而是发送一个 Lua 脚本，这个脚本只在客户端传入的值和键的口令串相匹配时，才对键进行删除。
     * 这两个改动可以防止持有过期锁的客户端误删现有锁的情况出现。
     */
    public Boolean unlock() {
        // 只有加锁成功并且锁还有效才去释放锁
        if (locked) {

            Long result;

            List<String> keys = new ArrayList<>();
            keys.add(lockKey);
            List<String> argv = new ArrayList<>();
            argv.add(lockValue);
            String scriptLoad = jedis.scriptLoad(UNLOCK_LUA);
            result = (Long) jedis.evalsha(scriptLoad, keys, argv);

            if (result == 0 && StringUtils.isNotBlank(lockKeyLog)) {
                LOGGER.info("Redis分布式锁，解锁{}失败！解锁时间：{}", lockKeyLog, System.currentTimeMillis());
            }

            locked = result == 0;
            return result == 1;
        }

        return true;
    }

    /**
     * 获取锁状态
     */
    public boolean isLock() {
        return locked;
    }

    /**
     * 重写Jedis的set方法
     * <p>
     * 命令 SET key value [EX seconds] [PX milliseconds] [NX|XX]。
     * <p>
     * 客户端执行以上的命令：
     * <p>
     * 如果服务器返回 OK ，那么这个客户端获得锁。
     * 如果服务器返回 NIL ，那么客户端获取锁失败，可以在稍后再重试。
     *
     * @param key 锁的Key
     * @param value 锁里面的值
     * @param seconds 过去时间（秒）
     * @return
     */
    private String set(final String key, final String value, final long seconds) {
        Assert.isTrue(StringUtils.isNotBlank(key), "key不能为空");
        String result = jedis.set(key, value, NX, EX, seconds);
        if (StringUtils.isNotBlank(lockKeyLog) && StringUtils.isNotBlank(result)) {
            LOGGER.info("获取锁{}的时间：{}", lockKeyLog, System.currentTimeMillis());
        }
        return result;
    }

    /**
     * 使当前执行的线程在指定的毫秒数和指定的纳秒数内休眠(临时停止执行)，
     * 这取决于系统计时器和调度器的精度和准确性。线程不会失去任何监视器的所有权。
     *
     * @param millis 毫秒
     * @param nanos 纳秒
     */
    private void seleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, random.nextInt(nanos));
        } catch (InterruptedException e) {
            LOGGER.info("获取分布式锁休眠被中断：", e);
        }
    }

    public String getLockKeyLog() {
        return lockKeyLog;
    }

    public void setLockKeyLog(String lockKeyLog) {
        this.lockKeyLog = lockKeyLog;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}
