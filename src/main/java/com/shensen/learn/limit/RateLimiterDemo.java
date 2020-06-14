package com.shensen.learn.limit;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiterDemo {

    public static void main(String[] args) {
        // 使用RateLimiter 限流
        RateLimiter rateLimiter = RateLimiter.create(10);

        ExecutorService executorService = Executors.newCachedThreadPool();
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < 10000; i++) {
            executorService.execute(() -> {
                // 判断能否在1秒内得到令牌，如果不能则立即返回false，不会阻塞程序
                boolean acquire = rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS);
                if (!acquire) {
                    System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
                } else {
                    count.getAndIncrement();
                }
            });
        }

        executorService.shutdown();

        while (true) {
            if (executorService.isTerminated()) {
                System.out.println("所有的线程都结束了！抢到令牌线程数：" + count.get());
                break;
            }
        }
    }
}
