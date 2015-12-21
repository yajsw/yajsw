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
package org.rzo.netty.mcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.util.internal.logging.InternalLogger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;

public class MulticastEndpoint
{

	private String mcastGroupIp = "228.10.10.10";
	private int mcastGroupPort = 12345;
	private String bindAddress = "192.168.0.10";

	private DatagramChannel datagramChannel;
	private Bootstrap connectionlessBootstrap;
	private InetSocketAddress multicastAddress;
	private static Executor executor = Executors.newCachedThreadPool();
	byte[] id;
	boolean init = false;
	EventLoopGroup group;
	public boolean debug = false;
	public InternalLogger logger;

	public void init(ChannelPipelineFactory factory) throws Exception
	{
		id = String.format("%1$020d",
				Math.abs(new Random(System.currentTimeMillis()).nextLong()))
				.getBytes();

		group = new OioEventLoopGroup();
		connectionlessBootstrap = new Bootstrap();
		connectionlessBootstrap.group(group);
		connectionlessBootstrap.option(ChannelOption.SO_BROADCAST, true);
		connectionlessBootstrap.handler(factory);
		connectionlessBootstrap.channel(OioDatagramChannel.class);
		;
		datagramChannel = (DatagramChannel) connectionlessBootstrap
				.bind(new InetSocketAddress(mcastGroupPort)).sync().channel();
		multicastAddress = new InetSocketAddress(mcastGroupIp, mcastGroupPort);
		NetworkInterface networkInterface = NetworkInterface
				.getByInetAddress(InetAddress.getByName(bindAddress));
		// for (Enumeration nifs = NetworkInterface.getNetworkInterfaces();
		// nifs.hasMoreElements(); )
		datagramChannel.joinGroup(multicastAddress, null);// (NetworkInterface)
															// nifs.nextElement());
		init = true;
		if (debug)
			factory.debug();
	}

	public boolean isInit()
	{
		return init;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public void setLogger(InternalLogger logger)
	{
		this.logger = logger;
	}

	public void send(ByteBuf msg) throws Exception
	{

		byte[] arr = msg.array();
		byte[] buf = new byte[arr.length + id.length];
		System.arraycopy(id, 0, buf, 0, id.length);
		System.arraycopy(arr, 0, buf, id.length, arr.length);

		ByteBuf bbuf = Unpooled.wrappedBuffer(buf);

		if (debug && logger != null)
			logger.info("discovery send " + new String(bbuf.array()));

		datagramChannel.writeAndFlush(
				new DatagramPacket(bbuf, multicastAddress)).sync();

		// datagramChannel.writeAndFlush(buf, multicastAddress);
	}

	public String getMcastGroupIp()
	{
		return mcastGroupIp;
	}

	public int getMcastGroupPort()
	{
		return mcastGroupPort;
	}

	public String getBindAddress()
	{
		return bindAddress;
	}

	public void setMcastGroupIp(String mcastGroupIp)
	{
		this.mcastGroupIp = mcastGroupIp;
	}

	public void setMcastGroupPort(int mcastGroupPort)
	{
		this.mcastGroupPort = mcastGroupPort;
	}

	public void setBindAddress(String bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	public void close()
	{
		datagramChannel.close();
		try
		{
			group.shutdownGracefully().sync();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public ByteBuf getMessage(ByteBuf e)
	{
		if (checkMessage(e))
		{
			return e.slice(id.length, e.readableBytes() - id.length);
		}
		return null;
	}

	public String getStringMessage(ByteBuf e)
	{
		ByteBuf m = getMessage(e);
		if (m == null)
			return null;
		return m.toString(Charset.defaultCharset());
	}

	public boolean checkMessage(ByteBuf e)
	{
		byte[] eId = new byte[id.length];
		e.getBytes(0, eId, 0, eId.length);
		return (!Arrays.equals(id, eId));
	}

}
