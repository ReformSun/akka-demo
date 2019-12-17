package com.test;

import akka.actor.*;
import akka.pattern.Patterns;
import com.sun.util.Time;
import com.test.akka.AkkaUtils;
import com.test.concurrent.FutureUtils;
import com.test.rpc.akka.AkkaInvocationHandler;
import com.test.rpc.akka.FencedAkkaInvocationHandler;
import com.test.rpc.akka.TestGateway;
import com.test.rpc.akka.TestGatewayImp;
import com.test.rpc.akka.messages.Processing;
import com.test.rpc.exceptions.RpcConnectionException;
import com.test.rpc.massage.HandshakeSuccessMessage;
import com.test.rpc.massage.RemoteHandshakeMessage;
import com.test.rpc.massage.RemoteRpcInvocation;
import org.apache.flink.api.java.tuple.Tuple2;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.reflect.ClassTag$;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * flink的这种rpc调用的设计模式，
 */
public class TestAkkaRpcActorC extends UntypedActor {
    @Override
    public void preStart() throws Exception {
        ActorSelection actorSelection = getContext().actorSelection("akka.tcp://strategy@127.0.0.1:8088/user/testAkkaRpcActor");
//        testMethod2(actorSelection);
//        testMethod1(actorSelection);
//        testMethod2_0(actorSelection);
//        testMethod2_1(actorSelection);
        testMethod3(actorSelection);
        super.preStart();
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        System.out.println("dd");
    }

    /**
     * 测试远程调用
     * 把要调用的方法名，方法参数类型，参数值，封装到remoteRpcInvocation方法中告诉服务端
     */
    public void testMethod1(ActorSelection actorSelection){

        String methodName = "testMethod";
        Class<?>[] parameterTypes = new Class[0];
        Object[] args = null;
        try {
            RemoteRpcInvocation remoteRpcInvocation = new RemoteRpcInvocation(methodName,parameterTypes,args);
            actorSelection.tell(remoteRpcInvocation,getSelf());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 普通调用
     */
    public  void testMethod2(ActorSelection actorSelection){
        //        actorSelection.tell("Hello PrintActor",getSelf());

        actorSelection.tell(Processing.START,getSelf());
    }

    public void testMethod2_0(ActorSelection actorSelection){
        Time timeout = Time.seconds(10);
        final Future<Object> identify = Patterns
                .ask(actorSelection, new Identify(42), timeout.toMilliseconds());
        try {
            Object result =  Await.result(identify, Duration.create(3, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testMethod2_1(ActorSelection actorSelection){
        Time timeout = Time.seconds(10);
        final Future<ActorIdentity> identify = Patterns
                .ask(actorSelection, new Identify(42), timeout.toMilliseconds())
                .<ActorIdentity>mapTo(ClassTag$.MODULE$.<ActorIdentity>apply(ActorIdentity.class));
        try {
            ActorIdentity result = (ActorIdentity) Await.result(identify, Duration.create(3, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void testMethod3(ActorSelection actorSelection){

        Class cla = TestRpcEndpoint.class;

        Time timeout = Time.milliseconds(10000);

        Function<ActorRef, InvocationHandler> invocationHandlerFunction = new Function<ActorRef, InvocationHandler>() {
            @Override
            public InvocationHandler apply(ActorRef actorRef) {
                Tuple2<String, String> addressHostname = new Tuple2<>("localhost","8088");

                return new AkkaInvocationHandler(
                        addressHostname.f0,
                        addressHostname.f1,
                        actorRef,
                        timeout,
                        Integer.MAX_VALUE,
                        null);
            }
        };

        final Future<ActorIdentity> identify = Patterns
                .ask(actorSelection, new Identify(42), timeout.toMilliseconds())
                .<ActorIdentity>mapTo(ClassTag$.MODULE$.<ActorIdentity>apply(ActorIdentity.class));

        final CompletableFuture<ActorIdentity> identifyFuture = FutureUtils.toJava(identify);

        final CompletableFuture<ActorRef> actorRefFuture = identifyFuture.thenApply(
                (ActorIdentity actorIdentity) -> {
                    if (actorIdentity.getRef() == null) {
                        throw new CompletionException(new RpcConnectionException("Could not connect to rpc endpoint under address " + actorSelection.pathString() + '.'));
                    } else {
                        return actorIdentity.getRef();
                    }
                });

//        try {
//            System.out.println(actorRefFuture.get().path());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
// 发送握手短信
        final CompletableFuture<HandshakeSuccessMessage> handshakeFuture = actorRefFuture.thenCompose(
                (ActorRef actorRef) -> FutureUtils.toJava(
                        Patterns
                                .ask(actorRef, new RemoteHandshakeMessage(cla, 1), timeout.toMilliseconds())
                                .<HandshakeSuccessMessage>mapTo(ClassTag$.MODULE$.<HandshakeSuccessMessage>apply(HandshakeSuccessMessage.class))));

//                try {
//            System.out.println(handshakeFuture.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        // 把试连接的结果和握手的结果做操作
        CompletableFuture<TestGateway> completableFuture = actorRefFuture.thenCombineAsync(
                handshakeFuture,
                (ActorRef actorRef, HandshakeSuccessMessage ignored) -> {
                    InvocationHandler invocationHandler = invocationHandlerFunction.apply(actorRef);

                    // Rather than using the System ClassLoader directly, we derive the ClassLoader
                    // from this class . That works better in cases where Flink runs embedded and all Flink
                    // code is loaded dynamically (for example from an OSGI bundle) through a custom ClassLoader
                    ClassLoader classLoader = getClass().getClassLoader();

                    @SuppressWarnings("unchecked")
                    TestGateway proxy = (TestGateway) Proxy.newProxyInstance(
                            classLoader,
                            new Class<?>[]{TestGateway.class},
                            invocationHandler);

                    return proxy;
                },
                getContext().dispatcher());

        try {
            TestGateway testGateway = completableFuture.get();
            testGateway.testMethod();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
}
