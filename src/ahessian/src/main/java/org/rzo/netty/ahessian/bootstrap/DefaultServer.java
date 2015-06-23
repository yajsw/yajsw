package org.rzo.netty.ahessian.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.Set;

public class DefaultServer extends DefaultEndpoint
{

	ServerBootstrap bootstrap;
    EventLoopGroup bossGroup;
    EventLoopGroup childGroup;
    EventExecutorGroup internalGroup;
    int _port;
    
    private static final int IPTOS_THROUGHPUT = 0x08;
    private static final int IPTOS_LOWDELAY = 0x10;        

	
	public DefaultServer(Class serverChannelClass, ChannelPipelineFactoryFactory factory, Set<String> channelOptions, int port)
	{
		if (!ServerChannel.class.isAssignableFrom(serverChannelClass))
			throw new RuntimeException("serverChannelClass must implement ServerChannel");

		// Configure the server.
        bootstrap = new ServerBootstrap();
        _port = port;
    	internalGroup = new DefaultEventExecutorGroup(10);
        
        if (isNio(serverChannelClass))
        {
        	bossGroup = new NioEventLoopGroup();
        	childGroup = new NioEventLoopGroup();
        }
        else if (isOio(serverChannelClass))
        {
        	bossGroup = new OioEventLoopGroup();
        	childGroup = new OioEventLoopGroup();
        }
        else
        {
        	bossGroup = new NioEventLoopGroup();
        	childGroup = new NioEventLoopGroup();
        }
        bootstrap.group(bossGroup, childGroup);
        bootstrap.channel(serverChannelClass); 
        //bootstrap.setOption("child.trafficClass", IPTOS_LOWDELAY);
        //bootstrap.setOption("child.tcpNoDelay", false);
        //bootstrap.childOption(ChannelOption.IP_TOS, IPTOS_THROUGHPUT);
        setChannelOptions(channelOptions);
        bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        ChannelPipelineFactory channelPipelineFactory = factory.create(internalGroup, bootstrap);
        bootstrap.childHandler(channelPipelineFactory);

	}
	
	
	private void setChannelOptions(Set<String> channelOptions)
	{
		if (channelOptions == null)
			return;
		// TODO add more options
		if (channelOptions.contains("IPTOS_THROUGHPUT"))
			 bootstrap.childOption(ChannelOption.IP_TOS, IPTOS_THROUGHPUT);
		else if (channelOptions.contains("IPTOS_LOWDELAY"))
			 bootstrap.childOption(ChannelOption.IP_TOS, IPTOS_LOWDELAY);	
		else if (channelOptions.contains("TCP_NODELAY"))
			 bootstrap.childOption(ChannelOption.TCP_NODELAY, true);	
		else if (channelOptions.contains("SO_REUSE"))
			 bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);	
		
	}

	@Override
	public
	void start() throws Exception
	{
		_channel = bootstrap.bind(new InetSocketAddress(_port)).sync().channel();
	}

	@Override
	void stop() throws Exception
	{
		getChannel().close().sync();
		bossGroup.shutdownGracefully();
		childGroup.shutdownGracefully();
		
	}

}
