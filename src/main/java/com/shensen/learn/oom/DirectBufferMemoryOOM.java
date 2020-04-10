package com.shensen.learn.oom;

import java.nio.ByteBuffer;

/**
 * 直接内存溢出：java.lang.OutOfMemoryError: Direct buffer memory
 * VM Args：-Xms10m -Xmx10m -XX:MaxDirectMemorySize=5m
 */
public class DirectBufferMemoryOOM {

    public static void main(String[] args) {
        ByteBuffer.allocateDirect(6 * 1024 * 1024);
    }
}
