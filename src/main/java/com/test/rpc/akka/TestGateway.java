package com.test.rpc.akka;


import org.apache.flink.runtime.rpc.RpcGateway;

public interface TestGateway extends RpcGateway {

	public void runAsync(Runnable runnable);

	public void testMethod();

}
