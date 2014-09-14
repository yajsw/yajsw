package org.rzo.netty.ahessian.bootstrap;

import io.netty.channel.Channel;

public abstract class DefaultEndpoint
{
	
	Channel _channel;

	protected boolean isOio(Class serverChannelClass)
	{
		// TODO this is quick and dirty
		return serverChannelClass.getName().contains("Oio");
	}

	protected boolean isNio(Class serverChannelClass)
	{
		// TODO this is quick and dirty
		return serverChannelClass.getName().contains("Nio");
	}

	abstract void start() throws Exception;
	
	abstract void stop() throws Exception;
	
	public Channel getChannel()
	{
		return _channel;
	}
}
