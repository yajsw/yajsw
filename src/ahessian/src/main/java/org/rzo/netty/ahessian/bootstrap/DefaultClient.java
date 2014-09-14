package org.rzo.netty.ahessian.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;

import java.net.ConnectException;
import java.util.Set;

import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler.ConnectListener;

public class DefaultClient<T> extends DefaultEndpoint
{
	Bootstrap bootstrap;
	EventLoopGroup internalGroup;
	EventLoopGroup workerGroup;
	ChannelPipelineFactoryBuilder<T> builder;
	private volatile boolean _stop = false;
	private volatile T _proxy;
	ChannelPipelineFactoryFactory _factory;
	
	public DefaultClient(Class channelClass, ChannelPipelineFactoryFactory factory, Set<String> channelOptions)
		{
			if (!Channel.class.isAssignableFrom(channelClass))
				throw new RuntimeException("serverChannelClass must implement ServerChannel");
			
			_factory = factory;
			if (factory instanceof ChannelPipelineFactoryBuilder)
				builder = (ChannelPipelineFactoryBuilder) factory;

			// Configure the client.
	        bootstrap = new Bootstrap();
	        
	        if (isNio(channelClass))
	        {
	        	workerGroup = new NioEventLoopGroup();
	        }
	        else if (isOio(channelClass))
	        {
	        	workerGroup = new OioEventLoopGroup();
	        }
	        else
	        {
	        	workerGroup = new DefaultEventLoopGroup();
	        }
	        bootstrap.group(workerGroup);
	        bootstrap.channel(channelClass);
	        internalGroup = new DefaultEventLoopGroup();
	        
		}
	
	public void setRemoteAddress(String host, int port)
	{
		bootstrap.remoteAddress(host, port);
	}
	
	public void start() throws Exception
	{
        bootstrap.handler(_factory.create(internalGroup, bootstrap));
		if (builder == null || !builder.hasReconnect())
			_channel = bootstrap.connect().sync().channel();
		else while (_channel == null && !_stop)
		try
		{
			_channel = bootstrap.connect().sync().channel();
		}
		catch (Exception ex)
		{
			if (ex instanceof ConnectException)
			{
				System.out.println(ex);
				Thread.sleep(builder._reconnectTimeout);
			}
		}
	}
	
	public void stop() throws Exception
	{
		_stop   = true;
		_channel.close().sync();
		workerGroup.shutdownGracefully();		
		internalGroup.shutdownGracefully();		
	}
	
	public void connectedListener(ConnectListener listener)
	{
		builder.connectedListener(listener);
	}

	public void disconnectedListener(ConnectListener listener)
	{
		builder.disconnectedListener(listener);
	}
	
	public T proxy() throws Exception
	{
		return builder.proxy();
	}
	
	public boolean isConnected()
	{
		return _channel != null && _channel.isActive();
	}

	public void close()
	{
		builder.close();
		if (getChannel() != null && getChannel().isActive())
			getChannel().close();

	}

	public void unblock()
	{
		builder.unblock();
	}


	
}
