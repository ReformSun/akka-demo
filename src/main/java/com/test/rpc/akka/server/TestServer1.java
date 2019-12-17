package com.test.rpc.akka.server;

import com.test.rpc.akka.AkkaRpcServiceUtils;
import com.test.rpc.akka.TestRpcEndpoint;
import com.test.rpc.inter.RpcServer;
import com.test.rpc.inter.RpcService;
import com.test.rpc.jobmaster.JobMasterGatewayImp;
import com.test.rpc.jobmaster.JobMasterGatewayNoFencedImp;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigurationUtils;

import java.util.Properties;

public class TestServer1 {
    public static void main(String[] args) {
        Properties propertie = new Properties();
        propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
//		propertie.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
//		propertie.setProperty("akka.remote.netty.tcp.port", "8082");
        Configuration configuration = ConfigurationUtils.createConfiguration(propertie);
        testMethod1(configuration);

    }

    /**
     * 创建rpc服务
     */
    public static void testMethod1(Configuration configuration){
        try {
            RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8082,configuration);
            // 创建一个rpc服务
            JobMasterGatewayNoFencedImp testRpcEndpoint = new JobMasterGatewayNoFencedImp(rpcService,"jobmaster");
            // 开始一个rpc服务
            testRpcEndpoint.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 剖析startServer方法
     *
     */
    public static void testMethod2(Configuration configuration){
        try {
            RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8082,configuration);
            RpcServer rpcServer = rpcService.startServer(new JobMasterGatewayImp(rpcService));
            rpcServer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
