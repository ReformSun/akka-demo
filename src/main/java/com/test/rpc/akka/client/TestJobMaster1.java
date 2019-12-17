package com.test.rpc.akka.client;

import com.test.rpc.akka.AkkaRpcServiceUtils;
import com.test.rpc.inter.RpcService;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigurationUtils;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestJobMaster1 {
    public static void main(String[] args) {
        Properties propertie = new Properties();
        propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
//		propertie.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
//		propertie.setProperty("akka.remote.netty.tcp.port", "8082");

        Configuration configuration = ConfigurationUtils.createConfiguration(propertie);

        try {
            RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8081,configuration);
            testMethod1(rpcService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 调用服务端的具体的某个方法
     * @param rpcService
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void testMethod1(RpcService rpcService) throws ExecutionException, InterruptedException {
        Class<JobMasterGateway> jobMasterGatewayClass = JobMasterGateway.class;
        CompletableFuture<JobMasterGateway> testEndpoint2 = rpcService.connect("akka.tcp://flink@127.0.0.1:8082/user/jobmaster",jobMasterGatewayClass);
        JobMasterGateway testGateway = testEndpoint2.get();

        testGateway.getAddress();
        System.out.println(testGateway.getAddress());
        testGateway.stop(Time.days(1));
        rpcService.stopService();

    }
}
