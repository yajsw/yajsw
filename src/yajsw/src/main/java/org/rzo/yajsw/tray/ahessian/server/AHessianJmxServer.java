package org.rzo.yajsw.tray.ahessian.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ipfilter.IpFilterRuleList;
import io.netty.util.internal.logging.InternalLogger;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;

import org.rzo.netty.ahessian.application.jmx.remote.service.JmxSerializerFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactoryBuilder;
import org.rzo.netty.ahessian.bootstrap.DefaultServer;
import org.rzo.netty.mcast.discovery.DiscoveryServer;

public class AHessianJmxServer
{
	public AHessianJmxServer(MBeanServer mbeanServer, String ipFilter, String serviceDiscoveryName, int port, InternalLogger logger, int debug) throws Exception
	{
		
		
    	ChannelPipelineFactoryBuilder builder = new ChannelPipelineFactoryBuilder()
    	.serializerFactory(new JmxSerializerFactory())
    	.rpcServiceInterface(MBeanServerConnection.class)
    	.rpcServerService(mbeanServer)
    	.serviceThreads(10)
    	.ipFilter(ipFilter);
    	
    	if (debug > 2)
    		builder.debug();
    	
    	Set<String> channelOptions = new HashSet();
    	//channelOptions.add("SO_REUSE");
    	channelOptions.add("TCP_NODELAY");

		int serverPort = port;

    	DefaultServer server = new DefaultServer(NioServerSocketChannel.class, builder, channelOptions, serverPort);

        server.start();
        Channel channel = server.getChannel();

		
		Executor executor = Executors.newCachedThreadPool();

		if (serverPort == 0)
			serverPort = ((InetSocketAddress) channel.localAddress()).getPort();

		if (debug > 2 && logger != null)
			logger.info("ahessian jmx service bound to port " + serverPort);

		DiscoveryServer discovery = new DiscoveryServer();
		discovery.setDebug(debug > 2);
		discovery.setLogger(logger);
		// allow discovery only from localhost. other computers will be ignored
		discovery.setIpSet(new IpFilterRuleList("+n:localhost, -n:*"));
		discovery.setName(serviceDiscoveryName);
		discovery.setPort(serverPort);
		discovery.init();

	}
	
	public static void main(String[] args) throws Exception
	{
		MBeanServer _mbeanServer = MBeanServerFactory.createMBeanServer();
		AHessianJmxServer _ahessianServer = new AHessianJmxServer(_mbeanServer, "+n:localhost, -n:*", "test", 15009, null, 0);

	}

}
