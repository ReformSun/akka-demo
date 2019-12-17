package com.test;

import com.test.rpc.inter.RpcEndpoint;
import com.test.rpc.inter.RpcService;

import java.util.concurrent.CompletableFuture;

public class SunCRpcEndpoint extends RpcEndpoint implements SunCGateway {
    protected SunCRpcEndpoint(RpcService rpcService, String endpointId) {
        super(rpcService, endpointId);
    }

    protected SunCRpcEndpoint(RpcService rpcService) {
        super(rpcService);
    }

    @Override
    public void testMethod1() {

    }

    @Override
    public void testMethod2() {

    }

    @Override
    public CompletableFuture<Void> postStop() {
        return null;
    }
}
