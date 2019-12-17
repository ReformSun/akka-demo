/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.rpc.akka;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.sun.util.Time;
import com.test.concurrent.FutureUtils;
import com.test.rpc.akka.messages.Processing;
import com.test.rpc.inter.*;
import com.test.rpc.massage.*;
import org.apache.flink.runtime.rpc.RpcGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.sun.util.Preconditions.checkArgument;
import static com.sun.util.Preconditions.checkNotNull;


/**
 * 调用句柄和AkkaRpcActor一起被使用，这个调用者句柄包裹这个rpc用一个LocalRpcInvocation消息
 * Invocation handler to be used with an {@link AkkaRpcActor}. The invocation handler wraps the
 * rpc in a {@link LocalRpcInvocation} message and then sends it to the {@link AkkaRpcActor} where it is
 * executed.
 */
public class AkkaInvocationHandler implements InvocationHandler, AkkaBasedEndpoint, RpcServer {
	private static final Logger LOG = LoggerFactory.getLogger(AkkaInvocationHandler.class);

	/**
	 * The Akka (RPC) address of {@link #rpcEndpoint} including host and port of the ActorSystem in
	 * which the actor is running.
	 */
	private final String address;

	/**
	 * Hostname of the host, {@link #rpcEndpoint} is running on.
	 */
	private final String hostname;

	private final ActorRef rpcEndpoint;

	// whether the actor ref is local and thus no message serialization is needed
	protected final boolean isLocal;

	// default timeout for asks
	private final Time timeout;

	private final long maximumFramesize;

	// null if gateway; otherwise non-null
	@Nullable
	private final CompletableFuture<Void> terminationFuture;

	public AkkaInvocationHandler(
			String address,
			String hostname,
			ActorRef rpcEndpoint,
			Time timeout,
			long maximumFramesize,
			@Nullable CompletableFuture<Void> terminationFuture) {

		this.address = checkNotNull(address);
		this.hostname = checkNotNull(hostname);
		this.rpcEndpoint = checkNotNull(rpcEndpoint);
		this.isLocal = this.rpcEndpoint.path().address().hasLocalScope();
		this.timeout = checkNotNull(timeout);
		this.maximumFramesize = maximumFramesize;
		this.terminationFuture = terminationFuture;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Class<?> declaringClass = method.getDeclaringClass();

		Object result;

		if (declaringClass.equals(AkkaBasedEndpoint.class) ||
			declaringClass.equals(Object.class) ||
			declaringClass.equals(RpcGateway.class) ||
			declaringClass.equals(StartStoppable.class) ||
			declaringClass.equals(MainThreadExecutable.class) ||
			declaringClass.equals(RpcServer.class)) {
			result = method.invoke(this, args);
		} else if (declaringClass.equals(FencedRpcGateway.class)) {
			throw new UnsupportedOperationException("AkkaInvocationHandler does not support the call FencedRpcGateway#" +
				method.getName() + ". This indicates that you retrieved a FencedRpcGateway without specifying a " +
				"fencing token. Please use RpcService#connect(RpcService, F, Time) with F being the fencing token to " +
				"retrieve a properly FencedRpcGateway.");
		} else {
			result = invokeRpc(method, args);
		}

		return result;
	}

	@Override
	public ActorRef getActorRef() {
		return rpcEndpoint;
	}

	@Override
	public void runAsync(Runnable runnable) {
		scheduleRunAsync(runnable, 0L);
	}

	@Override
	public void scheduleRunAsync(Runnable runnable, long delayMillis) {
		checkNotNull(runnable, "runnable");
		checkArgument(delayMillis >= 0, "delay must be zero or greater");

		if (isLocal) {
			long atTimeNanos = delayMillis == 0 ? 0 : System.nanoTime() + (delayMillis * 1_000_000);
			tell(new RunAsync(runnable, atTimeNanos));
		} else {
			throw new RuntimeException("Trying to send a Runnable to a remote actor at " +
				rpcEndpoint.path() + ". This is not supported.");
		}
	}

	@Override
	public <V> CompletableFuture<V> callAsync(Callable<V> callable, Time callTimeout) {
		if (isLocal) {
			@SuppressWarnings("unchecked")
			CompletableFuture<V> resultFuture = (CompletableFuture<V>) ask(new CallAsync(callable), callTimeout);

			return resultFuture;
		} else {
			throw new RuntimeException("Trying to send a Callable to a remote actor at " +
				rpcEndpoint.path() + ". This is not supported.");
		}
	}

	@Override
	public void start() {
		rpcEndpoint.tell(Processing.START, ActorRef.noSender());
	}

	@Override
	public void stop() {
		rpcEndpoint.tell(Processing.STOP, ActorRef.noSender());
	}

	// ------------------------------------------------------------------------
	//  Private methods
	// ------------------------------------------------------------------------

	/**
	 * 调用一个rpc方法通过一个发送的rpc调用详情从一个rpc终点
	 * Invokes a RPC method by sending the RPC invocation details to the rpc endpoint.
	 *
	 * @param method to call 被调用的方法
	 * @param args of the method call 被调用方法的参数值
	 * @return result of the RPC 调用之后的结果
	 * @throws Exception if the RPC invocation fails
	 */
	private Object invokeRpc(Method method, Object[] args) throws Exception {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Time futureTimeout = extractRpcTimeout(parameterAnnotations, args, timeout);

		final RpcInvocation rpcInvocation = createRpcInvocationMessage(methodName, parameterTypes, args);

		Class<?> returnType = method.getReturnType();

		final Object result;

		if (Objects.equals(returnType, Void.TYPE)) {
			tell(rpcInvocation);

			result = null;
		} else if (Objects.equals(returnType, CompletableFuture.class)) {
			// execute an asynchronous call
			result = ask(rpcInvocation, futureTimeout);
		} else {
			// execute a synchronous call
			CompletableFuture<?> futureResult = ask(rpcInvocation, futureTimeout);

			result = futureResult.get(futureTimeout.getSize(), futureTimeout.getUnit());
		}

		return result;
	}

	/**
	 * 创建一个rpc调用信息通过给予的rpc
	 * Create the RpcInvocation message for the given RPC.
	 * 创建对象需要方法名，参数类型，参数
	 * @param methodName of the RPC
	 * @param parameterTypes of the RPC
	 * @param args of the RPC
	 * @return RpcInvocation message which encapsulates the RPC details
	 * @throws IOException if we cannot serialize the RPC invocation parameters
	 *
	 * 创建两种类型的rpc调用
	 * LocalRpcInvocation {@link com.test.rpc.massage.LocalRpcInvocation}
	 * RemoteRpcInvocation {@link com.test.rpc.massage.RemoteRpcInvocation}
	 *
	 */
	protected RpcInvocation createRpcInvocationMessage(
			final String methodName,
			final Class<?>[] parameterTypes,
			final Object[] args) throws IOException {
		final RpcInvocation rpcInvocation;

		if (isLocal) {
			rpcInvocation = new LocalRpcInvocation(
				methodName,
				parameterTypes,
				args);
		} else {
			try {
				RemoteRpcInvocation remoteRpcInvocation = new RemoteRpcInvocation(
					methodName,
					parameterTypes,
					args);

				if (remoteRpcInvocation.getSize() > maximumFramesize) {
					throw new IOException("The rpc invocation size exceeds the maximum akka framesize.");
				} else {
					rpcInvocation = remoteRpcInvocation;
				}
			} catch (IOException e) {
				LOG.warn("Could not create remote rpc invocation message. Failing rpc invocation because...", e);
				throw e;
			}
		}

		return rpcInvocation;
	}

	// ------------------------------------------------------------------------
	//  Helper methods
	// ------------------------------------------------------------------------

	/**
	 * Extracts the {@link RpcTimeout} annotated rpc timeout value from the list of given method
	 * arguments. If no {@link RpcTimeout} annotated parameter could be found, then the default
	 * timeout is returned.
	 *
	 * @param parameterAnnotations Parameter annotations
	 * @param args Array of arguments
	 * @param defaultTimeout Default timeout to return if no {@link RpcTimeout} annotated parameter
	 *                       has been found
	 * @return Timeout extracted from the array of arguments or the default timeout
	 */
	private static Time extractRpcTimeout(Annotation[][] parameterAnnotations, Object[] args, Time defaultTimeout) {
		if (args != null) {
			checkArgument(parameterAnnotations.length == args.length);

			for (int i = 0; i < parameterAnnotations.length; i++) {
				if (isRpcTimeout(parameterAnnotations[i])) {
					if (args[i] instanceof Time) {
						return (Time) args[i];
					} else {
						throw new RuntimeException("The rpc timeout parameter must be of type " +
							Time.class.getName() + ". The type " + args[i].getClass().getName() +
							" is not supported.");
					}
				}
			}
		}

		return defaultTimeout;
	}

	/**
	 * Checks whether any of the annotations is of type {@link RpcTimeout}.
	 *
	 * @param annotations Array of annotations
	 * @return True if {@link RpcTimeout} was found; otherwise false
	 */
	private static boolean isRpcTimeout(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(RpcTimeout.class)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Sends the message to the RPC endpoint.
	 *
	 * @param message to send to the RPC endpoint.
	 */
	protected void tell(Object message) {
		rpcEndpoint.tell(message, ActorRef.noSender());
	}

	/**
	 * Sends the message to the RPC endpoint and returns a future containing
	 * its response.
	 *
	 * @param message to send to the RPC endpoint
	 * @param timeout time to wait until the response future is failed with a {@link TimeoutException}
	 * @return Response future
	 */
	protected CompletableFuture<?> ask(Object message, Time timeout) {
		return FutureUtils.toJava(
			Patterns.ask(rpcEndpoint, message, timeout.toMilliseconds()));
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public CompletableFuture<Void> getTerminationFuture() {
		return terminationFuture;
	}
}