package org.rzo.yajsw.srvmgr.server;

import org.rzo.netty.ahessian.application.jmx.remote.service.JmxSerializerFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultServer;
import org.rzo.netty.ahessian.rpc.server.ContinuationService;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler;
import org.rzo.netty.ahessian.rpc.server.ImmediateInvokeService;
import org.rzo.netty.mcast.discovery.DiscoveryServer;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.SimpleLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.management.MBeanServerConnection;

import org.rzo.yajsw.srvmgr.server.ms.win.WinServiceManagerServer;

public class ServerMain
{
	public static void main(String[] args) throws IOException, IOException, ClassNotFoundException, Exception
	{
		int serverPort = 0;
		try
		{
			serverPort = Integer.parseInt(args[0]);
		}
		catch (Exception ex)
		{
			// no port -> bind to 0 port and annouce port per multicast
		}
		String acl = null;
		int aclParPos = 1;
		if (serverPort == 0)
			aclParPos = 0;
			
		if (args.length == aclParPos+1)
			acl = args[aclParPos];
		List clientHosts = new ArrayList();
		File f = new File("serviceManagerServer.ser");
		if (f.exists())
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
			clientHosts = (List) in.readObject();
			in.close();
		}
		
    	ChannelPipelineFactoryBuilder builder = new ChannelPipelineFactoryBuilder()
    	.rpcServiceInterface(ServiceManagerServer.class)
    	.rpcServerService(getServiceManagerServer())
    	.serviceThreads(10);
    	
    	//if (debug)
    		builder.debug();
    	
    	Set<String> channelOptions = new HashSet();
    	channelOptions.add("TCP_NODELAY");

    	DefaultServer server = new DefaultServer(NioServerSocketChannel.class, builder, channelOptions, serverPort, null);

        server.start();
       Channel channel = server.getChannel();
       
       InetSocketAddress addr = (InetSocketAddress) channel.localAddress();
       serverPort = addr.getPort();

		
		
        DiscoveryServer discovery = new DiscoveryServer();
        discovery.setName("serviceManagerServer");
        discovery.setPort(serverPort);
        discovery.setDebug(true);
        discovery.setLogger(new SimpleLogger());
        discovery.init();
	}

	private static ServiceManagerServer getServiceManagerServer()
	{
		return new WinServiceManagerServer();
	}

}
