package com.test.rpc.akka;



import com.test.rpc.inter.RpcEndpoint;
import com.test.rpc.inter.RpcService;

import java.util.concurrent.CompletableFuture;

public class TestEndpoint2 extends RpcEndpoint {
	protected TestEndpoint2(RpcService rpcService, String endpointId) {
		super(rpcService, endpointId);
	}

	protected TestEndpoint2(RpcService rpcService) {
		super(rpcService);
	}

	@Override
	public CompletableFuture<Void> postStop() {
		return null;
	}
}
