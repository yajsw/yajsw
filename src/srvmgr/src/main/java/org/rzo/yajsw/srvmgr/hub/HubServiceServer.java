package org.rzo.yajsw.srvmgr.hub;

import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultClient;
import org.rzo.netty.ahessian.bootstrap.DefaultServer;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler;
import org.rzo.netty.ahessian.rpc.server.ImmediateInvokeService;
import org.rzo.yajsw.os.ServiceInfo;
import org.rzo.yajsw.srvmgr.client.AsyncServiceManagerServer;
import org.rzo.yajsw.srvmgr.client.Host;
import org.rzo.yajsw.srvmgr.server.ServiceManagerServer;

public class HubServiceServer implements HubService
{
	Map<String, DefaultClient> _proxies;
	
	Comparator<Host> hostsComparator = new Comparator<Host>()
	{
		public int compare(Host o1, Host o2)
		{
			return o1.getName().compareTo(o2.getName());
		}		
	};

	Comparator<ServiceInfo> servicesComparator = new Comparator<ServiceInfo>()
	{
		public int compare(ServiceInfo o1, ServiceInfo o2)
		{
			return o1.getName().compareTo(o2.getName());
		}		
	};

	HubServiceServer(int port, String acl, Map<String, DefaultClient>proxies) throws Exception
	{
		_proxies = proxies;
		
    	ChannelPipelineFactoryBuilder builder = new ChannelPipelineFactoryBuilder()
    	.rpcServiceInterface(HubService.class)
    	.rpcServerService(this)
    	.serviceThreads(10);
    	
    	//if (debug)
    		builder.debug();
    	
    	Set<String> channelOptions = new HashSet();
    	channelOptions.add("TCP_NODELAY");

    	DefaultServer server = new DefaultServer(NioServerSocketChannel.class, builder, channelOptions, port, null);

        server.start();
	}
	
	private  AsyncServiceManagerServer getProxy(String name)
	{
		DefaultClient client = _proxies.get(name);
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


	public List<Host> getHosts()
	{
		List<Host> result;
		synchronized(HubMain.hostsList)
		{
		  result = new ArrayList(HubMain.hostsList.values());
		}
		Collections.sort(result, hostsComparator);
		System.out.println("getHosts #"+result.size());
		return result;
	}

	public List<ServiceInfo> getServices()
	{
		List<ServiceInfo> result;
		synchronized(HubMain.servicesList)
		{
		  result = new ArrayList(HubMain.servicesList);
		}
		Collections.sort(result, servicesComparator);
		System.out.println("getServices #"+result.size());
		return result;
	}

	public void hide(String serviceName, String hostName)
	{
		synchronized(HubMain.hiddenList)
		{
		System.out.println("hiding "+serviceName);
		HubMain.hiddenList.add(serviceName);
		}
	}

	public void start(String serviceName, String hostName)
	{
		AsyncServiceManagerServer proxy = getProxy(hostName);
		if (proxy != null)
			proxy.start(serviceName);
	}

	public void stop(String serviceName, String hostName)
	{
		AsyncServiceManagerServer proxy = getProxy(hostName);
		if (proxy != null)
			proxy.stop(serviceName);
	}

}
