package com.test;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.sun.util.Time;
import com.test.concurrent.FutureUtils;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.reflect.ClassTag$;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by liubenlong on 2017/1/12.
 */
public class WorkerActor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final ActorSelection actorSelection = getContext().actorSelection("akka.tcp://strategy@127.0.0.1:8082/user/printActor");

    /**
     * 当一个角色被创建并且开始的之前的时候调用
     * @throws Exception
     */
    @Override
    public void preStart() throws Exception {
        testMethod1();
        super.preStart();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);
    }

    /**
     * 尝试连接actorSelection
     */
    public void testMethod1() {
        Time timeout = Time.milliseconds(10000);
        /**
         * actor寻址
         * actor的路径包含两部分:源:akka://ActorSystem;路径：/user/actor；
         * 如果我们向ActorSelection发送消息而其对应的Actor不存在，消息就会丢失.
         * 判断actor存在
         * 使用akka.actor.Identify
         */
        final Future<ActorIdentity> identify = Patterns
                .ask(actorSelection, new Identify(42), timeout.toMilliseconds())
                .<ActorIdentity>mapTo(ClassTag$.MODULE$.<ActorIdentity>apply(ActorIdentity.class));
        // 把scala的Future转成java的Future
        CompletableFuture<ActorIdentity> completableFuture = FutureUtils.toJava(identify);
        // 把CompletableFuture阶段完成时，再在所在线程执行Funtion方法
        CompletableFuture<ActorRef> completableFuture1 = completableFuture.thenApply(actorIdentity -> actorIdentity.getRef());

        try {
            // 堵塞当前线程，获取CompletableFuture阶段的执行的结果值
            ActorRef actorRef = completableFuture1.get();
            System.out.println(actorRef.path());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void testMethod2() {
        // 在这里也可以直接通过上下文，给定地址获取自己连接的对象
        // ActorSelection对象的理解：它是ActorSystem的Actors树的一部分的逻辑视图，允许向该部分广播消息。
        // 可以通过这个对象实现信息的发送

//        actorSelection.tell("Hello PrintActor",getSelf());
        Future<Object> future = Patterns.ask(actorSelection, "Hello PrintActor", 10000);
        String result = null;
        try {
            result = (String) Await.result(future, Duration.create(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        log.info("akka.future.WorkerActor.onReceive:" + o);
        // 得到发送者，并打印发送者路径
        System.out.println(getSender().path());
        // 打印自己的路径信息
        System.out.println(getSelf().path());
        if (o instanceof Integer) {
            Thread.sleep(1000);
            int i = Integer.parseInt(o.toString());
            getSender().tell(i*i, getSelf());
        } else {
            System.out.println("dd");
//            unhandled(o);
        }
    }

}
