/*******************************************************************************
 * Copyright  2015 rzorzorzo@users.sf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.rzo.netty.ahessian.application.jmx.remote.client;

import io.netty.channel.EventLoopGroup;

import java.util.concurrent.Executor;

import org.rzo.netty.ahessian.application.jmx.remote.service.JmxSerializerFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.io.InputStreamHandler;
import org.rzo.netty.ahessian.io.OutputStreamHandler;
import org.rzo.netty.ahessian.io.PullInputStreamConsumer;
import org.rzo.netty.ahessian.rpc.client.HessianProxyFactory;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallEncoder;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyDecoder;

import com.caucho.hessian4.io.SerializerFactory;

public class RPCClientMixinPipelineFactory extends ChannelPipelineFactory
{

	Executor _executor;
	HessianProxyFactory _factory;
	SerializerFactory _serializerFactory = new JmxSerializerFactory();

	RPCClientMixinPipelineFactory(Executor executor,
			HessianProxyFactory factory, EventLoopGroup group)
	{
		super(group);
		_executor = executor;
		_factory = factory;
	}

	public HandlerList getPipeline() throws Exception
	{
		HandlerList pipeline = new HandlerList();
		// ChannelHandlerInvoker invoker = new
		// DirectWriteChannelHandlerInvoker(getGroup().next());

		pipeline.addLast("inputStream", new InputStreamHandler());
		pipeline.addLast("outputStream", new OutputStreamHandler(), getGroup());
		pipeline.addLast("hessianReplyDecoder", new PullInputStreamConsumer(
				new HessianRPCReplyDecoder(_factory, _serializerFactory)),
				getGroup());
		pipeline.addLast("hessianCallEncoder", new HessianRPCCallEncoder(
				_serializerFactory, _executor), getGroup());
		pipeline.addLast("hessianHandler", _factory);

		return pipeline;
	}

}
