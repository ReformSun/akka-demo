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

package com.test.rpc.massage;

import java.io.IOException;

/**
 * 我的理解 RpcInvocation
 *  包含调用rpc调用信息的一个包装类
 *  里面包含所用调用的服务方的类的方法名，方法参数类型，方法参数值
 *  以方便服务方接受到这个包装类时，可以知道需要调用的类的方法
 *
 * Interface for rpc invocation messages. The interface allows to request all necessary information
 * to lookup a method and call it with the corresponding arguments.
 */
public interface RpcInvocation {

	/**
	 * Returns the method's name.
	 *
	 * @return Method name
	 * @throws IOException if the rpc invocation message is a remote message and could not be deserialized
	 * @throws ClassNotFoundException if the rpc invocation message is a remote message and contains
	 * serialized classes which cannot be found on the receiving side
	 */
	String getMethodName() throws IOException, ClassNotFoundException;

	/**
	 * Returns the method's parameter types
	 *
	 * @return Method's parameter types
	 * @throws IOException if the rpc invocation message is a remote message and could not be deserialized
	 * @throws ClassNotFoundException if the rpc invocation message is a remote message and contains
	 * serialized classes which cannot be found on the receiving side
	 */
	Class<?>[] getParameterTypes() throws IOException, ClassNotFoundException;

	/**
	 * Returns the arguments of the remote procedure call
	 *
	 * @return Arguments of the remote procedure call
	 * @throws IOException if the rpc invocation message is a remote message and could not be deserialized
	 * @throws ClassNotFoundException if the rpc invocation message is a remote message and contains
	 * serialized classes which cannot be found on the receiving side
	 */
	Object[] getArgs() throws IOException, ClassNotFoundException;
}
