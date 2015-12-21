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
package org.rzo.netty.mcast.bridge;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioDatagramChannel;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.mcast.MulticastEndpoint;

public class MulticastAdapter
{
	private static Channel channel;
	private static MulticastEndpoint mcast = new MulticastEndpoint();
	private static long RECONNECT_DELAY = 5000;
	private static Timer timer = new Timer();
	private static Bootstrap bootstrap;

	public static void main(String[] args) throws Exception
	{
		String host = args[0];
		int port = Integer.parseInt(args[1]);

		bootstrap = new Bootstrap();
		EventLoopGroup group = new OioEventLoopGroup();
		bootstrap.group(group);
		bootstrap.channel(OioDatagramChannel.class);

		bootstrap.remoteAddress(new InetSocketAddress(host, port));

		bootstrap.handler(new ChannelPipelineFactory()
		{
			public HandlerList getPipeline()
			{
				return ChannelPipelineFactory
						.handlerList(new ChannelInboundHandlerAdapter()
						{
							@Override
							public void channelRead(ChannelHandlerContext ctx,
									Object msg) throws Exception
							{
								if (mcast != null && mcast.isInit())
									mcast.send((ByteBuf) msg);
							}

							@Override
							public void channelActive(ChannelHandlerContext ctx)
							{
								timer.schedule(new TimerTask()
								{
									public void run()
									{
										bootstrap.connect();
									}
								}, RECONNECT_DELAY);
							}

							@Override
							public void exceptionCaught(
									ChannelHandlerContext ctx, Throwable e)
							{
								Throwable cause = e.getCause();
								if (cause instanceof ConnectException)
								{
									System.out
											.println("conection lost: reconnecting...");
								}
								ctx.channel().close();
							}

						});
			}
		});

		ChannelFuture f = bootstrap.connect();
		channel = f.sync().channel();

		mcast.init(new ChannelPipelineFactory()
		{
			public HandlerList getPipeline()
			{
				return ChannelPipelineFactory
						.handlerList(new ChannelInboundHandlerAdapter()
						{
							@Override
							public void channelRead(ChannelHandlerContext ctx,
									Object msg) throws Exception
							{
								ByteBuf b = mcast.getMessage((ByteBuf) msg);
								if (b == null)
									return;
								if (channel != null && channel.isActive())
									channel.write(b);
							}

						});
			}
		});

	}

}
