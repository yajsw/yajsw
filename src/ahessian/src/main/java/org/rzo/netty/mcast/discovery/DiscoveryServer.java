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
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.Iterator;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.mcast.MulticastEndpoint;

public class DiscoveryServer extends MulticastEndpoint
{
	private String name;
	private String host;
	private int port;
	private IpFilterRuleList firewall;

	public void init() throws Exception
	{
		if (host == null)
			host = whatIsMyIp();

		ChannelPipelineFactory factory = new ChannelPipelineFactory()
		{
			public HandlerList getPipeline() throws Exception
			{
				HandlerList pipeline = new HandlerList();
				pipeline.addLast("discoveryServer",
						new ChannelInboundHandlerAdapter()
						{

							@Override
							public void channelRead(ChannelHandlerContext ctx,
									Object e) throws Exception
							{
								// System.out.println("discovery server received "+e);
								String request = getStringMessage(((DatagramPacket) e)
										.content());
								InetSocketAddress remoteAddress = ((DatagramPacket) e)
										.sender();
								if (debug && logger != null)
									logger.info("discoveryServer messageReceived "
											+ request + "/" + remoteAddress);
								if (request == null)
									return;
								if (name != null && name.equals(request)
										&& host != null && port > 0)
								{
									if (validate(
											((DatagramPacket) e).content(),
											remoteAddress))
										send(Unpooled.wrappedBuffer((name + "&"
												+ host + "&" + port).getBytes()));
								}
								else if (debug && logger != null)
									logger.info("discoveryServer request rejected");
							}

						});
				return pipeline;
			}

		};
		super.init(factory);
	}

	public String getName()
	{
		return name;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setIpSet(IpFilterRuleList ipSet)
	{
		this.firewall = ipSet;
	}

	private String whatIsMyIp()
	{
		String result = null;
		try
		{
			Enumeration<NetworkInterface> e = NetworkInterface
					.getNetworkInterfaces();

			while (e.hasMoreElements())
			{
				NetworkInterface ne = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> e2 = ne.getInetAddresses();

				while (e2.hasMoreElements())
				{
					InetAddress ia = (InetAddress) e2.nextElement();

					if (!ia.isAnyLocalAddress() && !ia.isLinkLocalAddress()
							&& !ia.isLoopbackAddress()
							&& !ia.isMulticastAddress())
						if (result == null || !ia.isSiteLocalAddress())
						{
							result = ia.getHostAddress();
						}
				}
			}
		}
		catch (Exception ex)
		{
			Constants.ahessianLogger.warn("", ex);
		}
		result = "127.0.0.1";
		return result;

	}

	private boolean validate(ByteBuf e, SocketAddress socketAddress)
	{
		if (firewall == null)
			return true;
		else
		{
			InetAddress inetAddress = ((InetSocketAddress) socketAddress)
					.getAddress();
			Iterator<IpFilterRule> iterator = firewall.iterator();
			IpFilterRule ipFilterRule = null;
			while (iterator.hasNext())
			{
				ipFilterRule = iterator.next();
				if (ipFilterRule.contains(inetAddress))
				{
					// Match founds, is it a ALLOW or DENY rule
					boolean result = ipFilterRule.isAllowRule();
					if (debug && logger != null)
						logger.info("DiscoverServer firewall ip allowed: "
								+ result);
					return result;
				}
			}
			// No limitation founds and no allow either, but as it is like
			// Firewall rules, it is therefore accepted
			if (debug && logger != null)
				logger.info("DiscoverServer no firewall: ");
			return true;
		}
	}

	public static void main(String[] args)
	{
		DiscoveryServer server = new DiscoveryServer();
		server.setName("serviceManagerServer");
		server.setHost("localhost");
		server.setPort(8080);
		try
		{
			server.init();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
