package com.shensen.learn.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * GC回收时间过长时会抛出OutOfMemroyError。
 * 过长的定义：超过98%的时间用来做GC并且回收了不到2%的堆内存 ,连续多次GC都只回收了不到2%的极端情况下才会抛出。
 * 假如不抛出GC overhead Limit错误会发生什么情况呢?
 * 那就是GC清理的这么点内存很快会再次填满,追使GC再次执行.这样就形成恶性循环, CPU使用率一直是100%,而GC却没有任何成果
 * VM Args：-Xms10m -Xmx10m -XX:MaxDirectMemorySize=5m
 */
public class GCOverheadLimitExceededOOM {

    public static void main(String[] args) {
        int i = 0;
        List<String> list = new ArrayList<>();

        while (true) {
            list.add(String.valueOf(++i));
        }
    }
}
