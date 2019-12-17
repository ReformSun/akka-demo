package com.test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.sun.util.Time;
import com.test.akka.AkkaUtils;
import com.test.rpc.akka.AkkaInvocationHandler;
import com.test.rpc.akka.AkkaRpcActor;
import com.test.rpc.akka.AkkaRpcServiceUtils;
import com.test.rpc.akka.TestGatewayImp;
import com.test.rpc.inter.RpcServer;
import com.test.rpc.inter.RpcService;
import com.typesafe.config.ConfigFactory;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigurationUtils;
import scala.Option;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class TestMain3 {
    public static void main(String[] args) {
        try {
            testMethod1();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        testMethod2();
    }

    public static void testMethod2(){
        ActorSystem actorSystem = TestMainUtil.crearActorSystem("8088");
        CompletableFuture<Void> terminationFuture = new CompletableFuture<>();
        TestGatewayImp testGatewayImp = new TestGatewayImp();
        int version = 1;
        Props akkaRpcActorProps = Props.create(TestAkkaRpcActor.class, testGatewayImp, terminationFuture, version);
        ActorRef actorRef = actorSystem.actorOf(akkaRpcActorProps, "testAkkaRpcActor");
        actorSystem.awaitTermination();

    }

    public static void testMethod1() throws Exception {
        int version = 1;
        CompletableFuture<Void> terminationFuture = new CompletableFuture<>();
        Properties propertie = new Properties();
        propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
//		propertie.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
//		propertie.setProperty("akka.remote.netty.tcp.port", "8082");
        Configuration configuration = ConfigurationUtils.createConfiguration(propertie);
        RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8082,configuration);
        TestRpcEndpoint testRpcEndpoint = new TestRpcEndpoint(rpcService);
        ActorSystem actorSystem = TestMainUtil.crearActorSystem("8088");

        Props akkaRpcActorProps = Props.create(AkkaRpcActor.class, testRpcEndpoint, terminationFuture, version);

        ActorRef actorRef = actorSystem.actorOf(akkaRpcActorProps, "testAkkaRpcActor");
        System.out.println(actorRef.path());

        String akkaAddress = AkkaUtils.getAkkaURL(actorSystem, actorRef);
        final String hostname;
        Option<String> host = actorRef.path().address().host();
        if (host.isEmpty()) {
            hostname = "localhost";
        } else {
            hostname = host.get();
        }
        InvocationHandler akkaInvocationHandler  = new AkkaInvocationHandler(
                akkaAddress,
                hostname,
                actorRef,
                Time.milliseconds(100),
                Long.MAX_VALUE,
                terminationFuture);

        ClassLoader classLoader = testRpcEndpoint.getClass().getClassLoader();
        Class<?>[] interfaces = {RpcServer.class};
        RpcServer server = (RpcServer) Proxy.newProxyInstance(
                classLoader,
                interfaces,
                akkaInvocationHandler);
        server.start();

    }


}
