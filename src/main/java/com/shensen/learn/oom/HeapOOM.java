package com.shensen.learn.oom;

import org.apache.commons.lang3.StringUtils;

/**
 * Java堆溢出：java.lang.OutOfMemoryError: Java heap space
 * VM Args：-Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails
 */
public class HeapOOM {

    public static void main(String[] args) {
        /*List<OOMObject> list = new ArrayList<>();

        while (true) {
            list.add(new OOMObject());
        }*/
        byte[] b = new byte[30 * 1024 * 1024];
    }

    static class OOMObject {

    }
}
