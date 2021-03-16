package com.shensen.cycledepend;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * nested exception is org.springframework.beans.factory.BeanCurrentlyInCreationException:
 * Error creating bean with name 'a': 578624778
 * Requested bean is currently in creation: Is there an unresolvable circular reference?
 *
 *
 * 只有单例的bean会通过三级缓存提前暴露来解决循环依赖的问题，因为单例的时候只有一份，随时复用，那么就放到缓存里面
 * 而多例的bean，每次从容器中荻取都是—个新的对象，都会重B新创建，
 * 所以非单例的bean是没有缓存的，不会将其放到三级缓存中。
 */
public class SpringCycleDependTest {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        context.getBean("a", A.class);
        context.getBean("b", B.class);
    }
}