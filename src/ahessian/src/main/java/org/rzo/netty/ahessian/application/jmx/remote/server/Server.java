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
package org.rzo.netty.ahessian.application.jmx.remote.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
	public static void main(String[] args)
	{
		Executor executor = Executors.newFixedThreadPool(200);
		ServerBootstrap bootstrap = new ServerBootstrap();
		EventLoopGroup bossGroup = new OioEventLoopGroup();
		EventLoopGroup childGroup = new OioEventLoopGroup();
		bootstrap.group(bossGroup, childGroup);
		bootstrap.channel(OioServerSocketChannel.class);

		bootstrap.childHandler(new RPCServerSessionPipelineFactory(
				new RPCServerMixinPipelineFactory(executor, childGroup)));

		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(8080));

	}

}
