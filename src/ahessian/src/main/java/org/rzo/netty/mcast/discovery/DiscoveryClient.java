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
package org.rzo.netty.mcast.discovery;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleList;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.mcast.MulticastEndpoint;

public class DiscoveryClient extends MulticastEndpoint
{

	private String name;
	private Set<String> hosts = Collections
			.synchronizedSet(new HashSet<String>());
	private volatile boolean stop = false;
	private Set<DiscoveryListener> listeners = Collections
			.synchronizedSet(new HashSet<DiscoveryListener>());

	private static Executor executor = Executors.newCachedThreadPool();
	private IpFilterRuleList firewall;

	public void init() throws Exception
	{
		ChannelPipelineFactory factory = new ChannelPipelineFactory()
		{
			public HandlerList getPipeline() throws Exception
			{
				HandlerList pipeline = new HandlerList();
				pipeline.addLast("discoveryClient",
						new ChannelInboundHandlerAdapter()
						{

							@Override
							public void channelRead(ChannelHandlerContext ctx,
									Object e) throws Exception
							{
								try
								{
									String response = getStringMessage(((DatagramPacket) e)
											.content());
									InetSocketAddress remoteAddress = ((DatagramPacket) e)
											.sender();
									if (debug && logger != null)
										logger.info("discoveryClient messageReceived "
												+ response
												+ "/"
												+ remoteAddress);

									if (response == null)
										return;
									String[] resp = response.split("&");
									if (resp.length == 3)
									{
										String remoteName = resp[0];
										if (!name.equals(remoteName))
											return;
										if (!validate(
												((DatagramPacket) e).content(),
												remoteAddress))
											return;
										String host = resp[1];
										// check the name. if not valid will
										// cause an exception
										InetAddress.getByName(host);
										// get the port. if not a number will
										// cause an exception
										int port = Integer.parseInt(resp[2]);
										if (!hosts.contains(response))
										{
											hosts.add(response);
											for (DiscoveryListener listener : listeners)
											{
												listener.newHost(name, response);
											}
										}

									}
								}
								catch (Exception ex)
								{
									Constants.ahessianLogger.warn("", ex);
								}
							}
						});
				return pipeline;
			}

		};
		super.init(factory);

	}

	public void start() throws Exception
	{
		stop = false;
		discoverServices();
	}

	private void discoverServices() throws Exception
	{
		executor.execute(new Runnable()
		{
			public void run()
			{
				while (!stop)
				{
					try
					{
						send(Unpooled.copiedBuffer(name.getBytes()));
					}
					catch (Exception e)
					{
						Constants.ahessianLogger.warn("", e);
					}
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						Constants.ahessianLogger.warn("", e);
					}
				}
			}
		});
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void stop()
	{
		stop = true;
	}

	public void addListener(DiscoveryListener listener)
	{
		listeners.add(listener);
	}

	public void removeHost(String host)
	{
		hosts.remove(host);
	}

	private boolean validate(ByteBuf e, SocketAddress socketAddress)
	{
		if (firewall == null)
			return true;
		else
		{

			Iterator<IpFilterRule> iterator = firewall.iterator();
			IpFilterRule ipFilterRule = null;
			while (iterator.hasNext())
			{
				InetAddress inetAddress = ((InetSocketAddress) socketAddress)
						.getAddress();
				ipFilterRule = iterator.next();
				if (ipFilterRule.contains(inetAddress))
				{
					// Match founds, is it a ALLOW or DENY rule
					return ipFilterRule.isAllowRule();
				}
			}
			// No limitation founds and no allow either, but as it is like
			// Firewall rules, it is therefore accepted
			return true;
		}
	}

	public void setIpSet(IpFilterRuleList ipSet)
	{
		this.firewall = ipSet;
	}

	public static void main(String[] args) throws Exception
	{
		final DiscoveryClient client = new DiscoveryClient();
		client.setName("testService");
		client.addListener(new DiscoveryListener()
		{

			@Override
			public void newHost(String name, String host)
			{
				System.out.println("found service " + host + " " + name);
				client.stop();
			}
		});
		client.init();
		client.start();
	}

}
