package com.shensen.learn.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;

public class CommandHelloWorld extends HystrixCommand<Void> {

    private final String name;

    public CommandHelloWorld(String name) {
        super(HystrixCommandGroupKey.Factory.asKey("CommandHelloWorldGroup"));
        this.name = name;
    }

    @Override
    protected Void run() throws Exception {
        System.out.println("Hello " + name + "!");
        TimeUnit.MILLISECONDS.sleep(2);
        return null;
    }

    public static void main(String[] args) throws Exception {
        CommandHelloWorld command = new CommandHelloWorld("Bob");
        command.execute();

        command = new CommandHelloWorld("Hystrix");
        command.queue().get(10, TimeUnit.MILLISECONDS);

        // 注册观察者事件拦截  
        Observable<Void> observe = new CommandHelloWorld("Hystrix observe").observe();
        // 注册结果回调事件
        observe.subscribe(onNext -> {
            // 执行结果处理,result 为HelloWorldCommand返回的结果
            // 用户对结果做二次处理
        });
        
        // 注册完整执行生命周期事件
        observe.subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {
                // onNext/onError完成之后最后回调
                System.out.println("execute onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                // 当产生异常时回调
                System.out.println("onError " + e.getMessage());
            }

            @Override
            public void onNext(Void v) {
                // 获取结果后回调
                System.out.println("onNext: " + v);
            }
        });

    }
}