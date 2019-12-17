package com.test.rpc.akka;

import com.test.rpc.inter.RpcService;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigurationUtils;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;


import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestAkkaService2 {
	public static void main(String[] args) {
		Properties propertie = new Properties();
		propertie.setProperty("akka.actor.provider","akka.remote.RemoteActorRefProvider");
//		propertie.setProperty("akka.remote.netty.tcp.hostname", "127.0.0.1");
//		propertie.setProperty("akka.remote.netty.tcp.port", "8082");

		Configuration configuration = ConfigurationUtils.createConfiguration(propertie);

		try {
			RpcService rpcService = AkkaRpcServiceUtils.createRpcService("127.0.0.1",8081,configuration);
			testMethod1(rpcService);
//			testMethod2(rpcService);
//			testMethod3(rpcService);
//			testMethod4(rpcService);
//			testMethod5(rpcService);

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
		CompletableFuture<TestGateway> testEndpoint2 = rpcService.connect("akka.tcp://flink@127.0.0.1:8082/user/testRpcEndpoint",TestGateway.class);
		TestGateway testGateway = testEndpoint2.get();

		testGateway.getAddress();
		System.out.println(testGateway.getAddress());
		testGateway.testMethod();
		rpcService.stopService();


	}
	public static void testMethod1_1(RpcService rpcService) throws ExecutionException, InterruptedException {
		CompletableFuture<JobMasterGateway> testEndpoint2 = rpcService.connect("akka.tcp://flink@127.0.0.1:8082/user/testRpcEndpoint",JobMasterGateway.class);
		JobMasterGateway testGateway = testEndpoint2.get();

		testGateway.getAddress();
		System.out.println(testGateway.getAddress());
		rpcService.stopService();


	}



	public static void testMethod3(RpcService rpcService){
		CompletableFuture<TestGateway> testEndpoint2 = rpcService.connect("akka.tcp://flink@127.0.0.1:8082/user/testRpcEndpoint",TestGateway.class);
		CompletableFuture<String> completableFuture = testEndpoint2.handleAsync((a,b) ->{
			a.testMethod();
			return "ddd";
		});
		try {
			System.out.println(completableFuture.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		rpcService.stopService();
	}

	public static void testMethod4(RpcService rpcService){
	    rpcService.execute(() -> {
			System.out.println("ddd");
		});
	}
	/**
	 * 调用服务端的Runnable
	 */
	public static void testMethod5(RpcService rpcService){
		CompletableFuture<TestGateway> testRpcEndpointCompletableFuture = rpcService.connect("akka.tcp://flink@127.0.0.1:8082/user/testRpcEndpoint",TestGateway.class);
		try {
			TestGateway testRpcEndpoint = testRpcEndpointCompletableFuture.get();
			testRpcEndpoint.runAsync(() -> {
				System.out.println("ddd");
			});
			rpcService.stopService();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
