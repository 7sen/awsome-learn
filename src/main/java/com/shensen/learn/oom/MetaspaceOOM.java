package com.shensen.learn.oom;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * 方法区内存溢出：java.lang.OutOfMemoryError: Metaspace
 * VM Args：
 * -XX:PermSize=10M -XX:MaxPermSize=10M -XX:+PrintGCDetails
 * -XX:MetaspaceSize=10m -XX:MaxMetaspaceSize=10m -XX:+PrintGCDetails
 */
public class MetaspaceOOM {

    public static void main(String[] args) {
        while (true) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(OOMObject.class);
            enhancer.setUseCache(false);
            enhancer.setCallback(
                    (MethodInterceptor) (obj, method, objects, methodProxy) -> methodProxy.invokeSuper(obj, args));
            enhancer.create();
        }
    }

    static class OOMObject {

    }
}