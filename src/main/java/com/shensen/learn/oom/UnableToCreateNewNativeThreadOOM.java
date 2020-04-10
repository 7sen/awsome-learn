package com.shensen.learn.oom;

import java.util.concurrent.TimeUnit;

/**
 * Java.lang.OutOfMemeoryError:unable to create new native thread
 * 代码需要跑在Linux环境下，Linux系统非root用户默认创建线程个数1024
 * 配置文件路径：/etc/security/limits.d/20-nproc.conf
 */
public class UnableToCreateNewNativeThreadOOM {

    public static void main(String[] args) {
        for (int i = 0; ; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName());
                try {
                    TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, i + "").start();
        }
    }
}
