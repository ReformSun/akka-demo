package com.test.rpc.akka;

public class TestGatewayImp implements TestGateway{

	@Override
	public void runAsync(Runnable runnable) {

	}

	@Override
	public void testMethod() {
		System.out.println("cccccccc");
	}

	@Override
	public String getAddress() {
		return "dd";
	}

	@Override
	public String getHostname() {
		return "ccc";
	}
}
