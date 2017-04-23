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
package org.rzo.netty.ahessian.application.cluster.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ipfilter.IpFilterRuleList;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rzo.netty.ahessian.application.cluster.client.ClusterClient;
import org.rzo.netty.ahessian.application.cluster.service.ClusterService.ClusterEventListener;
import org.rzo.netty.ahessian.application.cluster.service.ClusterService.Member;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultClient;
import org.rzo.netty.ahessian.bootstrap.DefaultServer;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler.ConnectListener;
import org.rzo.netty.mcast.discovery.DiscoveryClient;
import org.rzo.netty.mcast.discovery.DiscoveryListener;
import org.rzo.netty.mcast.discovery.DiscoveryServer;

public class ClusterServer
{
	int _serverPort = 0;
	String _clusterName;
	String _clientName;
	Map<InetSocketAddress, DefaultClient> _seedClients = new ConcurrentHashMap<InetSocketAddress, DefaultClient>();
	List<InetSocketAddress> _seeds = new CopyOnWriteArrayList<InetSocketAddress>();
	String _ipFilter;
	DiscoveryServer _discoveryServer;
	DiscoveryClient _discovery;
	AtomicBoolean _connectingSeeds = new AtomicBoolean(false);
	static Executor _executor = Executors.newCachedThreadPool();
	ClusterServiceImpl _service;
	Member _me;
	InetSocketAddress _myAddress;
	String _myHost;
	InetAddress _address;

	public class ServerListener implements ClusterEventListener
	{
		@Override
		public void setDone(boolean value)
		{
		}

		@Override
		public boolean isDone()
		{
			return false;
		}

		@Override
		public boolean isValid()
		{
			return false;
		}

		@Override
		public void joined(Member member)
		{
			if (member.getName() != _me.getName())
				_service.seedJoin(member, null);
		}

		@Override
		public void left(Member member)
		{
			_service.leave(member.getName());
		}

	}

	public ClusterServer() throws Exception
	{
		this(null, 0, true, true, null, "DefaultCluster", ClusterClient.getHostName()
				+ "-" + System.currentTimeMillis(), new InetSocketAddress[0]);
	}

	public ClusterServer(int port) throws Exception
	{
		this(null, port, false, false, null, "DefaultCluster", ClusterClient
				.getHostName() + "-" + System.currentTimeMillis(),
				new InetSocketAddress[0]);

	}

	public ClusterServer(int port, InetSocketAddress... seeds) throws Exception
	{
		this(null, port, false, false, null, "DefaultCluster", ClusterClient
				.getHostName() + "-" + System.currentTimeMillis(), seeds);

	}

	public ClusterServer(InetAddress address, int port, boolean useDiscoveryServer,
			boolean useClientDiscovery, String ipFilter, String clusterName,
			String clientName, InetSocketAddress... seeds) throws Exception
	{
		// InternalLoggerFactory.setDefaultFactory(new SimpleLoggerFactory());
		_serverPort = port;
		_address = address;
		_ipFilter = ipFilter;
		_clusterName = clusterName;
		_clientName = clientName;

		_service = new ClusterServiceImpl();
		_service.setClusterName(_clusterName);
		_myHost = ClusterClient.getHostName();
		_me = _service.createMember(_clusterName, _clientName, null, true,
				_myHost);
		_service.setServer(_me);

		ChannelPipelineFactoryBuilder builder = new ChannelPipelineFactoryBuilder()
				.rpcServiceInterface(SeedClusterService.class)
				.rpcServerService(_service).serviceThreads(10)
				.serverHeartbeat(45000).debug().ipFilter(ipFilter);

		builder.disconnectedListener(new ConnectListener()
		{

			@Override
			public void run()
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void run(Channel channel)
			{
				if (_service != null)
					_service.disconnected(channel);
			}

		});

		Set<String> channelOptions = new HashSet();
		channelOptions.add("TCP_NODELAY");

		_serverPort = port;

		DefaultServer server = new DefaultServer(NioServerSocketChannel.class,
				builder, channelOptions, _serverPort, _address);

		server.start();
		Channel channel = server.getChannel();
		_myAddress = ((InetSocketAddress) channel.localAddress());
		_serverPort = _myAddress.getPort();

		if (seeds != null)
			for (InetSocketAddress seed : seeds)
			{
				if (!isMyAddress(seed))
					_seeds.add(seed);
			}

		if (useDiscoveryServer)
		{
			_discoveryServer = new DiscoveryServer();
			_discoveryServer.setDebug(true);
			// _discoveryServer.setLogger(new SimpleLogger());
			if (_ipFilter != null)
				_discoveryServer.setIpSet(new IpFilterRuleList(_ipFilter));
			_discoveryServer.setName(clusterName);
			_discoveryServer.setPort(_serverPort);
			_discoveryServer.init();
		}

		if (useClientDiscovery)
		{
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
					InetSocketAddress address = new InetSocketAddress(hostName,
							port);
					if (_seeds.contains(address) || isMyAddress(address))
						return;
					_seeds.add(address);
					connectSeeds();
				}
			});
			_discovery.init();

		}

		connectSeeds();

	}

	protected boolean isMyAddress(InetSocketAddress address)
	{
		try
		{
			return _myAddress.getPort() == address.getPort()
					&& (isLocalhost(address) || _myHost.equals(InetAddress
							.getByName(address.getHostName()).getAddress()));
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private boolean isLocalhost(InetSocketAddress address)
	{
		try
		{
			if (address.getHostName().equals(
					InetAddress.getLocalHost().getHostName()))
				return true;
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		try
		{
			InetAddress[] addrs = InetAddress.getAllByName("127.0.0.1");
			for (InetAddress addr : addrs)
				if (addr.getHostName().equals(address.getHostName()))
					return true;
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			if (interfaces != null)
				for (NetworkInterface in : Collections.list(interfaces))
				{
					for (InterfaceAddress addr : in.getInterfaceAddresses())
						if (addr.getAddress().getHostName()
								.equals(address.getHostName()))
							return true;

				}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;

	}

	protected void connectSeeds()
	{
		_executor.execute(new Runnable()
		{

			@Override
			public void run()
			{
				if (!_connectingSeeds.compareAndSet(false, true))
					return;

				boolean connectedToAll = false;
				while (!connectedToAll)
				{
					connectedToAll = true;
					for (InetSocketAddress address : _seeds)
					{
						if (_seedClients.get(address) == null)
							connectedToAll = connectedToAll
									&& connectToSeed(address);
					}
					if (!connectedToAll)
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
							break;
						}
				}
				_connectingSeeds.set(false);
			}
		});
	}

	private boolean connectToSeed(final InetSocketAddress seed)
	{
		System.out.println("connect to seed " + seed);
		Map options = new HashMap();
		options.put("sync", true);
		options.put("timeout", (long) 2000);

		final ChannelPipelineFactoryBuilder<SeedClusterService> builder = new ChannelPipelineFactoryBuilder<SeedClusterService>()
				.serviceThreads(10)
				// .reconnect(10)
				.rpcServiceInterface(SeedClusterService.class)
				.clientHeartbeat(30000).serviceOptions(options);

		builder.debug();

		final Set<String> channelOptions = new HashSet();
		// channelOptions.add("SO_REUSE");
		channelOptions.add("TCP_NODELAY");
		final DefaultClient<SeedClusterService> client = new DefaultClient<SeedClusterService>(
				NioSocketChannel.class, builder, channelOptions);
		client.setRemoteAddress(seed.getHostName(), seed.getPort());
		client.connectedListener(new ConnectListener()
		{

			@Override
			public void run()
			{
				try
				{
					client.proxy().seedJoin(_me, new ServerListener());
					_seedClients.put(seed, client);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
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
				_seedClients.remove(client.getChannel().remoteAddress());
				connectSeeds();
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
		return true;
	}

	private void disconnect()
	{
		/*
		 * DefaultClient client = _remoteClient.get(); if (client != null)
		 * client.close(); _remoteClient.set(null);
		 */
	}

	public List<Member> getMembers()
	{
		return _service.getMembers();
	}

	public static void main(String[] args) throws Exception
	{
		ClusterServer server1 = new ClusterServer(15010, new InetSocketAddress(
				"127.0.0.1", 15010), new InetSocketAddress("127.0.0.1", 15011),
				new InetSocketAddress("127.0.0.1", 15012));
		ClusterServer server2 = new ClusterServer(15011, new InetSocketAddress(
				"127.0.0.1", 15010), new InetSocketAddress("127.0.0.1", 15011),
				new InetSocketAddress("127.0.0.1", 15012));
		ClusterServer server3 = new ClusterServer(15012, new InetSocketAddress(
				"127.0.0.1", 15010), new InetSocketAddress("127.0.0.1", 15011),
				new InetSocketAddress("127.0.0.1", 15012));

		while (true)
		{
			System.out.println(server1.getMembers());
			Thread.sleep(5000);
		}
	}
}
