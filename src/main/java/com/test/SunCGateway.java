package com.test;

import org.apache.flink.runtime.rpc.RpcGateway;

/**
 * 在这里提供需要服务的接口名称
 */
public interface SunCGateway extends RpcGateway {
    /**
     * 注册服务
     */
    public void testMethod1();

    /**
     * 报告心跳的服务接口
     */
    public void testMethod2();
}
