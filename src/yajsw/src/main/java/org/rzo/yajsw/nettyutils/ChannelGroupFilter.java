package org.rzo.yajsw.nettyutils;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChannelGroupFilter extends ChannelOutboundHandlerAdapter
{
	ChannelGroup	_channels	= new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	Condition		_condition;

	public ChannelGroupFilter(Condition condition)
	{
		_condition = condition;
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
	throws Exception
	{
		_channels.add(ctx.channel());
		ctx.fireChannelWritabilityChanged();
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
	{
		if (_condition.isOk(ctx, msg))
		{
			_channels.remove(ctx.channel());
			_channels.close();
		}
		ctx.write(msg, promise);
	}

}
