package org.rzo.yajsw.srvmgr.hub;


import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultClient;
import org.rzo.netty.ahessian.rpc.client.HessianProxyFactory;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler.ConnectListener;
import org.rzo.netty.mcast.discovery.DiscoveryClient;
import org.rzo.netty.mcast.discovery.DiscoveryListener;
import org.rzo.netty.mcast.discovery.DiscoveryServer;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.rzo.yajsw.os.ServiceInfo;
import org.rzo.yajsw.os.ServiceInfoImpl;
import org.rzo.yajsw.srvmgr.client.AsyncServiceManagerServer;
import org.rzo.yajsw.srvmgr.client.Host;
import org.rzo.yajsw.srvmgr.server.ServiceManagerServer;

public class HubMain
{
	static List<ServiceInfo> servicesList = new ArrayList<ServiceInfo>();
	static Map<String, Host> hostsList = Collections.synchronizedMap(new HashMap<String, Host>());
	static Set<String> hiddenList = new HashSet();
	static Map<String, DefaultClient>proxies = Collections.synchronizedMap( new HashMap<String, DefaultClient>() );
	static Set<String> configurations =  new HashSet<String>();
	static ExecutorService executor = Executors.newCachedThreadPool();
    static DiscoveryClient discovery = new DiscoveryClient();
    static HubServiceServer hubServiceServer;


	public static void main(String[] args) throws Exception
	{
	    ExecutorService executor = Executors.newCachedThreadPool();
	    
	    loadData();
	    
	    new Timer("hosts updater", true).schedule(new TimerTask()
	    {

			@Override
			public void run()
			{
		        updateHosts();
		        updateServices();
			}

	    	
	    }, 0, 500);


        discovery.setName("serviceManagerServer");
        discovery.addListener(new DiscoveryListener()
        {

			public void newHost(String serviceName, String host)
			{
				try
				{
					String[] x = host.split(":");
				int port = Integer.parseInt(x[1]);
				String name = InetAddress.getByName(x[0]).getHostName();
				synchronized(hostsList)
				{
					System.out.println("new host "+name+":"+port);
					Host newHost = new Host(name, port);
					Host oldHost = hostsList.get(newHost.getName());
					if (oldHost != null)
					{
						newHost.setIncluded(oldHost.isIncluded());
					}
				hostsList.put(newHost.getName(), newHost);

	    		saveData();
				}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
			}
        	
        });
        discovery.init();
        discovery.start();


        hubServiceServer = new HubServiceServer(Integer.parseInt(args[0]), null, proxies);

	}

	protected static void doDeleteHost(String host)
	{
		synchronized(hostsList)
		{
		synchronized(servicesList)
		{
			hostsList.remove(host);
			for (ServiceInfo service : new ArrayList<ServiceInfo>(servicesList))
				if (host.equals(service.getHost()))
						servicesList.remove(service);
		saveData();
		}
		}
	}
	

	protected static void doNewHost(String host, int port)
	{
		synchronized(hostsList)
		{
			Host newHost = new Host(host, port);
			Host oldHost = hostsList.get(newHost.getName());
			if (oldHost != null)
			{
				newHost.setIncluded(oldHost.isIncluded());
			}
			newHost.setState("CONNECTED");
			hostsList.put(newHost.getName(), newHost);

		saveData();
		}

    	
	}

	private static void saveData()
	{
		Map data = new HashMap();
		data.put("hosts", hostsList);
		data.put("hidden", hiddenList);
		data.put("configurations", configurations);
		File f = new File("ServiceManager.ser");
		try
		{
		if (!f.exists())
			f.createNewFile();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(data);
		out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	private static void loadData()
	{
		File f = new File("ServiceManagerHub.ser");
		try
		{
		if (!f.exists())
			return;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
		Map data = (Map) in.readObject();
		for (Iterator it = ((Collection)data.get("hosts")).iterator(); it.hasNext(); )
		{
			Host host = (Host) it.next();
			hostsList.put(host.getName(), host);
		}
		for (Iterator it = ((Collection)data.get("hidden")).iterator(); it.hasNext(); )
			hiddenList.add((String)it.next());
		configurations = (Set<String>) data.get("configurations");
		in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
		
	}
	
	private static void updateServices()
	{
		synchronized(hostsList)
		{
		synchronized(proxies)
			{

		synchronized(servicesList)
		{
		servicesList.clear();
		for (String host : hostsList.keySet())
		{
			AsyncServiceManagerServer proxy = getProxy(host);
			Collection<ServiceInfo> services = null;
			try
			{
				 services = (Collection<ServiceInfo>) ((Map<String, ServiceInfo>) proxy.getServiceList()).values();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if (services != null)
			for (ServiceInfo service : services)
			{
				synchronized(hiddenList)
				{
				if (! hiddenList.contains(service.getName()))
				{
					((ServiceInfoImpl)service).setHost(host);
					servicesList.add(service);
				}
				}
			}
		}
		}
		}
		}
		System.out.println("update services: #"+servicesList.size());
	}
	
	private static AsyncServiceManagerServer getProxy(String name)
	{
		DefaultClient client = proxies.get(name);
		if (client == null)
			return null;
		try
		{
			return (AsyncServiceManagerServer) client.proxy();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}




	private static void updateHosts() 
	{
		boolean changed = false;
		
		Collection<Host> list;
		synchronized(hostsList)
		{
			list = new ArrayList(hostsList.values());
		}
		{
		for (Host host :list)
		{
			AsyncServiceManagerServer proxy = getProxy(host.getName());
			boolean connected = false;
			if (proxy != null)
			{
				try
				{
					connected = ((Boolean)((Future)proxy.isServiceManager()).get(10, TimeUnit.SECONDS)).booleanValue();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!connected)
			{
				
				final ChannelPipelineFactoryBuilder<MBeanServerConnection> builder = new ChannelPipelineFactoryBuilder<MBeanServerConnection>()
						.serviceThreads(10) //.reconnect(10)
						.rpcServiceInterface(AsyncServiceManagerServer.class)
						//.serviceOptions(options)
						;
				
				builder.debug();

				final Set<String> channelOptions = new HashSet();
				channelOptions.add("SO_REUSE");
				channelOptions.add("TCP_NODELAY");
				final DefaultClient client = new DefaultClient<MBeanServerConnection>(
						NioSocketChannel.class, builder, channelOptions);
				client.setRemoteAddress(host.getName(), host.getPort());
				final Host mHost = host;
				proxies.put(mHost.getName(), client);
				
				client.connectedListener(new ConnectListener()
				{
					
					@Override
					public void run()
					{
						executor.execute(new Runnable()
						{
							
							@Override
							public void run()
							{
								try
								{
								AsyncServiceManagerServer proxy = (AsyncServiceManagerServer) client.proxy();
								boolean connected = ((Boolean)((Future)proxy.isServiceManager()).get(10, TimeUnit.SECONDS)).booleanValue();
								if (connected)
								{
								Host newHost = new Host(mHost.getName(), mHost.getPort());
								newHost.setIncluded(mHost.isIncluded());
								newHost.setState("CONNECTED");
								hostsList.remove(mHost.getName());
								hostsList.put(newHost.getName(), newHost);
								//if (mHost.isIncluded())
								//	servicesTable.addService(mHost.getName(), proxy);
								}
								else
									client.close();
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
								}
							}
						});
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
						disconnect(mHost, client);
					}

					@Override
					public void run(Channel channel)
					{
						// TODO Auto-generated method stub
						
					}
				});
				
				try
				{
					System.out.println("start client: "+host.getName()+":"+host.getPort());
					client.start();
					connected = true;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

				
			if (!connected)
			{
				disconnect(host, proxies.remove(host.getName()));
				changed = true;
			}
			else if (proxy == null && !"DISCONNECTED".equals(host.getState()))
			{
				Host newHost = new Host(host.getName(), host.getPort());
				newHost.setIncluded(host.isIncluded());
				newHost.setState("DISCONNECTED");
				hostsList.put(newHost.getName(), newHost);
			}
		}
		}

		
	}
	
	private static void removeServices(String host)
	{
		
	}
	
	private static void disconnect(Host host, DefaultClient client)
	{
		if (client != null)
		{
			client.close();
			removeServices(host.getName());
		}
		host.setState("DISCONNECTED");
		try
		{
			discovery.removeHost(InetAddress.getByName(host.getName()).getHostAddress()+":"+host.getPort());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	



}
