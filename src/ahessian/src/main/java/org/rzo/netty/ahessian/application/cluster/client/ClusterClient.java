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
package org.rzo.netty.ahessian.application.cluster.client;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.rzo.netty.ahessian.application.cluster.service.ClusterService;
import org.rzo.netty.ahessian.application.cluster.service.ClusterService.ClusterEventListener;
import org.rzo.netty.ahessian.application.cluster.service.ClusterService.Member;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultClient;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler.ConnectListener;
import org.rzo.netty.mcast.discovery.DiscoveryClient;
import org.rzo.netty.mcast.discovery.DiscoveryListener;

public class ClusterClient
{
	List<InetSocketAddress> _seeds;
	AtomicReference<DefaultClient<ClusterService>> _remoteClient = new AtomicReference<DefaultClient<ClusterService>>();
	String _clientName;
	String _clusterName;
	boolean _useDiscovery = false;
	volatile boolean _stop = true;
	static Executor _executor = Executors.newCachedThreadPool();
	DiscoveryClient _discovery;
	Object _data;
	AtomicBoolean _connecting = new AtomicBoolean(false);
	AtomicBoolean _discovering = new AtomicBoolean(false);
	ClusterEventListener _listener;

	public ClusterClient(String clusterName, String myName,
			InetSocketAddress... seeds) throws Exception
	{
		// InternalLoggerFactory.setDefaultFactory(new SimpleLoggerFactory());
		_clusterName = clusterName;
		_clientName = myName;
		if (seeds == null || seeds.length == 0)
		{
			_useDiscovery = true;
			_seeds = new CopyOnWriteArrayList<InetSocketAddress>();
			_discovery = new DiscoveryClient();
			_discovery.setName(clusterName);
			_discovery.setDebug(true);
			// _discovery.setLogger(new SimpleLogger());
			_discovery.addListener(new DiscoveryListener()
			{

				@Override
				public void newHost(String name, String host)
				{
					String[] x = host.split("&");
					int port = Integer.parseInt(x[2]);
					String hostName = x[1];
					InetSocketAddress remoteAddress = (new InetSocketAddress(
							hostName, port));
					if (!_seeds.contains(remoteAddress))
					{
						_seeds.add(remoteAddress);
						System.out.println("adding seed " + remoteAddress);
						if (!isConnected())
							connect();
					}
				}
			});
			_discovery.init();
		}
		else
			_seeds = Arrays.asList(seeds);
	}

	public ClusterClient(InetSocketAddress... seeds) throws Exception
	{
		this("DefaultCluster", createName(), seeds);
	}

	public ClusterClient(String clusterName, String clientName)
			throws Exception
	{
		this(clusterName, clientName, new InetSocketAddress[0]);
	}

	public ClusterClient(String clusterName) throws Exception
	{
		this(clusterName, createName());
	}

	public ClusterClient() throws Exception
	{
		this("DefaultCluster", createName());
	}

	public void setData(Object data)
	{
		_data = data;
	}

	public void setListener(ClusterEventListener listener)
	{
		_listener = listener;
	}

	synchronized public void join() throws Exception
	{
		if (!_stop)
			return;
		_stop = false;
		if (_useDiscovery)
			startDiscovery();
		connect();
	}

	synchronized public void leave()
	{
		if (_stop)
			return;
		_stop = true;
		if (_useDiscovery)
			stopDiscovery();
		disconnect();
	}

	public boolean isConnected()
	{
		return _remoteClient.get() != null;
	}

	public List<Member> getMembers()
	{
		DefaultClient client = _remoteClient.get();
		if (client == null)
			return null;

		ClusterService proxy = null;
		;
		try
		{
			proxy = (ClusterService) client.proxy();
		}
		catch (Exception e)
		{
			// we may not have a connection or loose connection
		}
		if (proxy == null)
			return null;
		try
		{
			return proxy.getMembers();
		}
		catch (Exception ex)
		{
			// we may not have a connection or loose connection or get timeout
		}
		return null;
	}

	static private String createName()
	{
		return getHostName() + "-" + System.currentTimeMillis();
	}

	static public String getHostName()
	{
		String hostname = "Unknown";

		try
		{
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostAddress();
		}
		catch (UnknownHostException ex)
		{
			System.out.println("Hostname can not be resolved");
		}
		return hostname;
	}

	private void connect()
	{
		_executor.execute(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					connectInternal();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private void connectInternal() throws Exception
	{
		if (!_connecting.compareAndSet(false, true))
			return;
		while (!_stop && !isConnected())
		{
			for (InetSocketAddress seed : _seeds)
			{
				if (_stop || connectToSeed(seed))
					break;
			}
			if (!_stop && !isConnected())
				Thread.sleep(5000);
		}
		_connecting.set(false);

	}

	private boolean connectToSeed(final InetSocketAddress seed)
	{
		Map options = new HashMap();
		options.put("sync", true);
		options.put("timeout", (long) 200000);

		final ChannelPipelineFactoryBuilder<ClusterService> builder = new ChannelPipelineFactoryBuilder<ClusterService>()
				.serviceThreads(10)
				// .reconnect(10)
				.rpcServiceInterface(ClusterService.class)
				.clientHeartbeat(30000).serviceOptions(options);

		builder.debug();

		final Set<String> channelOptions = new HashSet();
		// channelOptions.add("SO_REUSE");
		channelOptions.add("TCP_NODELAY");
		final DefaultClient<ClusterService> client = new DefaultClient<ClusterService>(
				NioSocketChannel.class, builder, channelOptions);
		client.setRemoteAddress(seed.getHostName(), seed.getPort());
		client.connectedListener(new ConnectListener()
		{

			@Override
			public void run()
			{
				try
				{
					client.proxy().join(_clusterName, _clientName, _data,
							_listener);
					_remoteClient.compareAndSet(null, client);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void run(Channel channel)
			{
				// TODO Auto-generated method stub

			}
		});
		client.disconnectedListener(new ConnectListener()
		{

			@Override
			public void run()
			{
				try
				{
					client.proxy().leave(_clientName);
				}
				catch (Exception ex)
				{
					// do nothing connection may be already closed
				}
				client.close();
				if (!_stop && _remoteClient.compareAndSet(client, null))
					connect();
			}

			@Override
			public void run(Channel channel)
			{
				// TODO Auto-generated method stub

			}
		});
		try
		{
			client.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _remoteClient.get() != null;

	}

	private void disconnect()
	{
		DefaultClient<ClusterService> client = _remoteClient.get();
		if (client != null)
		{
			try
			{
				client.proxy().leave(_clientName);
			}
			catch (Exception e)
			{
				// may have already closed connection
			}
			client.close();
		}
		_remoteClient.set(null);
	}

	private void startDiscovery() throws Exception
	{
		if (!_discovering.compareAndSet(false, true))
			return;
		_discovery.start();
	}

	private void stopDiscovery()
	{
		if (!_discovering.compareAndSet(true, false))
			return;
		_discovery.stop();
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println(createName());
		ClusterClient cluster = new ClusterClient(new InetSocketAddress(
				"127.0.0.1", 15010), new InetSocketAddress("127.0.0.1", 15011),
				new InetSocketAddress("127.0.0.1", 15012));
		cluster.join();
		while (!cluster.isConnected())
			Thread.sleep(100);
		System.out.println("my name " + cluster._clientName);
		System.out.println("members " + cluster.getMembers());
		/*
		 * cluster.leave(); ClusterClient cluster2 = new ClusterClient(new
		 * InetSocketAddress( "127.0.0.1", 15010)); cluster2.join(); while
		 * (!cluster2.isConnected()) Thread.sleep(100);
		 * System.out.println("my name "+cluster2._clientName);
		 * System.out.println("members "+cluster2.getMembers());
		 * cluster2.leave();
		 */

		Thread.sleep(10000);
		System.exit(0);
	}

}
