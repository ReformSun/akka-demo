package com.test;

import akka.actor.*;
import akka.pattern.Patterns;
import com.test.concurrent.FutureUtils;
import com.test.rpc.exceptions.RpcConnectionException;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.reflect.ClassTag$;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * Props.create
 * 根据传入的类类型，构造函数参数，在生成角色时，生成实例化对象
 */
public class TestMain2 {
    public static void main(String[] args) {
//        ActorSystem system = TestMainUtil.crearActorSystem();
        try {
//            testMethod1(system);
//            testMethod1_0(system);
//            testMethod1_1(system);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        testMethod2(system);
//        testMethod3(system);
        testMethod4();
    }

    public static void testMethod1(ActorSystem system) throws Exception{
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class),"printActor");
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class),"workerActor");

        //等等future返回，向worker角色发送消息，超时时间设为10秒钟
        Future<Object> future = Patterns.ask(workerActor, 5, 10000);
        Integer result = (Integer) Await.result(future, Duration.create(3, TimeUnit.SECONDS));
        System.out.println("result:" + result);
    }

    public static void testMethod1_0(ActorSystem system){
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class),"printActor");
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class),"workerActor");

//        不等待返回值，直接重定向到其他actor，有返回值来的时候将会重定向到printActor
        Future<Object> future1 = Patterns.ask(workerActor, 8, 10000);
        Patterns.pipe(future1, system.dispatcher()).to(printActor);
//
//
//        workerActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    /**
     * mapto 当Future<s>是运行结果，但是使用mapto后会把s强制转换成S返回Future<S>
     * @param system
     */
    public static void testMethod1_1(ActorSystem system){
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class),"printActor");
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class),"workerActor");
        Future<ActorIdentity> future1 = Patterns.ask(workerActor, new Identify(42), 10000).mapTo(ClassTag$.MODULE$.apply(ActorIdentity.class));
        // 转化scala语言的Future到java语言的Future
        final CompletableFuture<ActorIdentity> identifyFuture = FutureUtils.toJava(future1);
        final CompletableFuture<ActorRef> actorRefFuture = identifyFuture.thenApply(
                (ActorIdentity actorIdentity) -> {
                    if (actorIdentity.getRef() == null) {
                        throw new CompletionException(new RpcConnectionException("Could not connect to rpc endpoint under address " + " "+ '.'));
                    } else {
                        return actorIdentity.getRef();
                    }
                });
//        try {
//            ActorIdentity actorIdentity = Await.result(future1,Duration.create(3, TimeUnit.SECONDS));
//            System.out.println(actorIdentity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 创建一个角色。并通过这个角色的实例化对象向其发送消息
     */
    public static void testMethod2(ActorSystem system){
        // 创建一个角色
        ActorRef workerActor =  system.actorOf(Props.create(WorkerActor.class),"workerActor");
        // 向这个角色发送信息,无发送者
        workerActor.tell(5,ActorRef.noSender());
        // 阻塞当前线程
//        system.awaitTermination();
    }

    /**
     * 创建两个对象。并通过实例化对象相互之间发送消息
     */
    public static void testMethod3(ActorSystem system){
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class),"printActor");
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class),"workerActor");
       // 向workerActor实例发送消息，指定发送者为printActor
        workerActor.tell(5,printActor);

//        workerActor.tell(5,ActorRef.noSender());

//        workerActor.tell(new RunAsyn(new Runnable() {
//            public void run() {
//                System.out.println("dd");
//            }
//        },0),printActor);

    }

    /**
     * 创建Print服务
     */
    public static void testMethod4(){
        ActorSystem system = TestMainUtil.crearActorSystem("8082");
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class),"printActor");
        system.awaitTermination();
    }

}
