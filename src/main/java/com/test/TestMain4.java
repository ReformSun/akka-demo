package com.test;

import com.test.rpc.akka.AkkaRpcServiceUtils;
import com.test.rpc.akka.TestRpcEndpoint;
import com.test.rpc.inter.RpcService;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigurationUtils;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class TestMain4 {
    public static void main(String[] args) {

    }

    /**
     * 使用flink rpc 创建一个远程服务
     * 1 创建一个提供接口的服务 这些接口决定了这个服务端所能提供的服务，但是自定的接口要继承自{@link com.test.rpc.inter.RpcGateway}接口
     * 比如{@link com.test.SunCGateway}
     *
     * 2 创建一个类 继承于{@link com.test.rpc.inter.RpcEndpoint} 这个抽象类非常重要 并且还要实现SunCGateway接口的方法
     * 比如 {@link com.test.SunCRpcEndpoint}
     *
     * 3 通过工具类{@link com.test.rpc.akka.AkkaRpcServiceUtils}创建服务
     *
     * 4 初始化SunCRpcEndpoint对象
     *
     * 5 启动服务
     *
     *
     * 服务端也是非常重要的 当调用RpcService{@link com.test.rpc.inter.RpcService}的startServer方法时
     *
     *
     * {@link com.test.rpc.inter.RpcServer} 这个接口的理解
     * 这个接口功能的实现还是主要靠{@link com.test.rpc.akka.AkkaInvocationHandler}类
     * 因为这个对象的生成也是通过Proxy.newProxyInstance反射的方式
     * 这个接口主要的功能的理解
     * 控制本地服务的状态 开始或者停止{@link com.test.rpc.akka.messages.Processing}
     * 包含了服务地址的信息
     *
     * 只可以指定一些操作，让本地服务在主线程中执行
     *
     *
     *
     *
     */
    public static void testMethod1() {
        Properties propertie = new Properties();
        propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
//		propertie.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
//		propertie.setProperty("akka.remote.netty.tcp.port", "8082");
        Configuration configuration = ConfigurationUtils.createConfiguration(propertie);
        try {
            RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8082,configuration);
            // 创建一个rpc服务
            SunCRpcEndpoint testRpcEndpoint = new SunCRpcEndpoint(rpcService,"testRpcEndpoint");
            // 开始一个rpc服务
            testRpcEndpoint.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 访问服务
     * 1 通过工具类创建本地服务
     * 2 通过服务实例化对象与远程服务建立连接
     *
     *
     * 在这里的执行流程的学习
     * 带我们通过服务的连接方法获取CompletableFuture<SunCGateway> completableFuture时，
     * 其实这个阶段完成后的值是一个通过Proxy.newProxyInstance这个方法创建的实例化对象{@link java.lang.reflect.Proxy}
     * 当我们调用SunCGateway内的方法时，其实是调用的AkkaInvocationHandler{@link com.test.rpc.akka.AkkaInvocationHandler}
     * 实例化对象的invoke方法
     * 在这个实例中拥有一个比较重要的对象ActorRef rpcEndpoint 它就是可以直接与服务方交流的实例化对象，
     * 当调用invoke方法时，就可以把调用的方法信息比如方法名，方法参数类型，方法参数包装进RpcInvocation{@link com.test.rpc.massage.RpcInvocation}
     * 实例化对象中在调用Patterns.ask(rpcEndpoint, message, timeout.toMilliseconds())，实现与服务端的联系
     *
     */
    public static void testMethod2() {
        Properties propertie = new Properties();
        propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
//		propertie.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
//		propertie.setProperty("akka.remote.netty.tcp.port", "8082");

        Configuration configuration = ConfigurationUtils.createConfiguration(propertie);

        try {
            RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8081,configuration);
            CompletableFuture<SunCGateway> completableFuture = rpcService.connect("akka.tcp://flink@127.0.0.1:8082/user/testRpcEndpoint",SunCGateway.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
