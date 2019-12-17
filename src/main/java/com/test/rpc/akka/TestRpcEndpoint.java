package com.test.rpc.akka;

//import org.apache.flink.runtime.rpc.RpcEndpoint;
//import org.apache.flink.runtime.rpc.RpcService;

import com.sun.util.Time;
import com.test.rpc.inter.RpcEndpoint;
import com.test.rpc.inter.RpcService;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class TestRpcEndpoint extends RpcEndpoint implements TestGateway{

	protected TestRpcEndpoint(RpcService rpcService, String endpointId) {
		super(rpcService, endpointId);
	}

	protected TestRpcEndpoint(RpcService rpcService) {
		super(rpcService);
	}

	@Override
	public CompletableFuture<Void> postStop() {
		return null;
	}

	@Override
	public void testMethod() {
		System.out.println("dd");
	}

//	@Override
//	public String getAddress() {
//		return "dd";
//	}


	@Override
	public void runAsync(Runnable runnable) {
		super.runAsync(runnable);
	}

	@Override
	protected <V> CompletableFuture<V> callAsync(Callable<V> callable, Time timeout) {
		return super.callAsync(callable, timeout);
	}

	@Override
	public String getEndpointId() {
		return super.getEndpointId();
	}
}
