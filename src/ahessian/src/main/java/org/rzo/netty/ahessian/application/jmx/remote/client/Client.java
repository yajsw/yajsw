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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.rzo.netty.ahessian.application.jmx.remote.service.AsyncMBeanServerConnection;
import org.rzo.netty.ahessian.application.jmx.remote.service.MBeanServerConnectionAsyncAdapter;
import org.rzo.netty.ahessian.rpc.client.HessianProxyFactory;

public class Client
{
	static boolean stop = false;
	static MBeanServerConnection server;

	public static void main(String[] args) throws Exception
	{

		// InternalLoggerFactory.setDefaultFactory(new SimpleLoggerFactory());
		final ExecutorService executor = Executors.newCachedThreadPool();

		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup workerGroup = new OioEventLoopGroup();
		// workerGroup.setIoRatio(99);
		bootstrap.group(workerGroup);
		bootstrap.channel(OioSocketChannel.class);

		bootstrap.remoteAddress(new InetSocketAddress("localhost", 15009));
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);

		final HessianProxyFactory factory = new HessianProxyFactory(executor,
				"localhost:15009");
		bootstrap.handler(new RPCClientSessionPipelineFactory(
				new RPCClientMixinPipelineFactory(executor, factory,
						workerGroup), bootstrap));

		factory.setDisconnectedListener(new Runnable()
		{
			public void run()
			{
				// stop = true;
			}
		});

		factory.setNewSessionListener(new Runnable()
		{
			public void run()
			{
				stop = false;
				executor.execute(new Runnable()
				{
					public void run()
					{
						System.out.println("started work thread");
						Map options = new HashMap();
						options.put("sync", true);
						options.put("timeout", (long) 10000);
						AsyncMBeanServerConnection service = (AsyncMBeanServerConnection) factory
								.create(AsyncMBeanServerConnection.class,
										Client.class.getClassLoader(), options);
						server = new MBeanServerConnectionAsyncAdapter(service);

						while (!stop)
						{
							try
							{
								ObjectName on = new ObjectName(
										"java.lang:type=ClassLoading");
								Object x = server.getAttribute(on,
										"LoadedClassCount");
								System.out.println(x);
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
								System.out.println(ex);
							}
							try
							{
								Thread.sleep(1000);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						System.out.println("stopped work thread");
					}
				});
			}
		});

		// Start the connection attempt.
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(
				"localhost", 15009));
		// Wait until the connection attempt succeeds or fails.
		Channel channel = future.awaitUninterruptibly().channel();
		if (future.isSuccess())
			System.out.println("connected");

		// get a proxy

	}

}
