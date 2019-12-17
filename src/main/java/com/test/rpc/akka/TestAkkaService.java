package com.test.rpc.akka;

import com.test.rpc.inter.RpcServer;
import com.test.rpc.inter.RpcService;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigurationUtils;
//import org.apache.flink.runtime.rpc.RpcService;

import java.util.Properties;


/**
 * RpcServer{@link com.test.rpc.inter.RpcServer} 接口是一个向外提供服务的一个接口
 *
 *
 *
 *
 */
public class TestAkkaService {
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
			TestRpcEndpoint testRpcEndpoint = new TestRpcEndpoint(rpcService,"testRpcEndpoint");
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
			RpcServer rpcServer = rpcService.startServer(new TestRpcEndpoint(rpcService));
			rpcServer.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
