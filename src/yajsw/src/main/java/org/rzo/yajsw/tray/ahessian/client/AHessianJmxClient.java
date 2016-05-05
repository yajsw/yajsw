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
package org.rzo.yajsw.tray.ahessian.client;

import io.netty.channel.Channel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.rzo.netty.ahessian.application.jmx.remote.service.JmxSerializerFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultClient;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler.ConnectListener;
import org.rzo.netty.ahessian.utils.MyReentrantLock;
import org.rzo.netty.mcast.discovery.DiscoveryClient;
import org.rzo.netty.mcast.discovery.DiscoveryListener;

public class AHessianJmxClient
{
	boolean stop = false;
	DiscoveryClient discovery = null;
	ExecutorService executor = Executors.newCachedThreadPool();
	volatile MBeanServerConnection mbeanServer = null;
	final Lock lock = new MyReentrantLock();
	final Condition connected = lock.newCondition();
	String currentHost = null;
	DefaultClient<MBeanServerConnection> client;
	Runnable _connectListener;
	Runnable _disconnectListener;
	String _discoveryName;
	int _port;
	boolean _debug = false;
	InternalLogger _logger;

	public AHessianJmxClient(String discoveryName, int port, boolean debug,
			InternalLogger logger) throws Exception
	{
		_discoveryName = discoveryName;
		_port = port;
		_debug = debug;
		_logger = logger;
		Map options = new HashMap();
		options.put("sync", true);
		options.put("timeout", (long) 5000);

		final ChannelPipelineFactoryBuilder<MBeanServerConnection> builder = new ChannelPipelineFactoryBuilder<MBeanServerConnection>()
				.serviceThreads(10).reconnect(10)
				.rpcServiceInterface(MBeanServerConnection.class)
				.serviceOptions(options);

		builder.debug();
		builder.serializerFactory(new JmxSerializerFactory());

		final Set<String> channelOptions = new HashSet();
		channelOptions.add("SO_REUSE");
		channelOptions.add("TCP_NODELAY");
		client = new DefaultClient<MBeanServerConnection>(
				OioSocketChannel.class, builder, channelOptions);

	}

	private void doConnected()
	{

		// if we do not have a port: use discovery
		lock.lock();
		// we will be using a synchronous service
		try
		{
			mbeanServer = client.proxy();
			connected.signal();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lock.unlock();
		if (_connectListener != null)
			_connectListener.run();
	}

	public void start() throws Exception
	{
		client.disconnectedListener(new ConnectListener()
		{
			public void run()
			{
				try
				{
					System.out.println("disconnected listener");
					close();
					mbeanServer = null;
					if (discovery != null)
					{
						if (currentHost != null)
							discovery.removeHost(currentHost);
						discovery.start();
					}
					if (_disconnectListener != null)
						_disconnectListener.run();
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
		client.connectedListener(new ConnectListener()
		{

			public void run()
			{
				// if (discovery == null)
				doConnected();
			}

			@Override
			public void run(Channel channel)
			{
				// TODO Auto-generated method stub

			}

		});
		if (_port == 0)
		{
			discovery = new DiscoveryClient();

			discovery.setName(_discoveryName);
			discovery.addListener(new DiscoveryListener()
			{
				// we have discovered our mbean server
				public void newHost(String name, String host)
				{
					try
					{
						// get the port - hostName should be the local host
						String[] x = host.split("&");
						int port = Integer.parseInt(x[2]);
						String hostName = x[1];
						// try to connect
						client.setRemoteAddress(hostName, port);
						client.start();
						// future.await(10000);

						// stop discovery
						discovery.stop();
						// doConnected();
						currentHost = host;
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			});
			discovery.init();
			discovery.start();
		}
		// if we have a port connect to that port
		else
		{
			client.setRemoteAddress("127.0.0.1", _port);
			client.start();
		}

	}

	public MBeanServerConnection getMBeanServer()
	{
		while (mbeanServer == null && !stop)

		{
			lock.lock();
			try
			{
				connected.await(1000, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			lock.unlock();
		}

		return mbeanServer;
	}

	public void close()
	{
		client.close();
	}

	public void open()
	{
		client.unblock();
	}

	public void stop()
	{
		stop = true;
	}

	public static void main(String[] args) throws Exception
	{
		// InternalLoggerFactory.setDefaultFactory(new SimpleLoggerFactory());

		AHessianJmxClient _ahessianClient = new AHessianJmxClient("test",
				15009, false, null);
		_ahessianClient.start();
		MBeanServerConnection jmxc = _ahessianClient.getMBeanServer();
		MBeanServerDelegateMBean proxy = (MBeanServerDelegateMBean) MBeanServerInvocationHandler
				.newProxyInstance(jmxc, new ObjectName(
						"JMImplementation:type=MBeanServerDelegate"),
						MBeanServerDelegateMBean.class, false);
		System.out.println(proxy.getMBeanServerId());
		boolean ok = false;
		while (true)
			try
			{
				System.out.println(proxy.getMBeanServerId());
				ok = true;
				Thread.sleep(1000);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Thread.sleep(1000);
			}

	}

	public void setConnectListener(Runnable connectListener)
	{
		_connectListener = connectListener;
	}

	public void setDisconnectListener(Runnable disconnectListener)
	{
		_disconnectListener = disconnectListener;
	}

}
