package org.rzo.yajsw.nettyutils;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;

public class ChannelGroupFilter extends ChannelHandlerAdapter
{
	ChannelGroup	_channels	= new DefaultChannelGroup(new DefaultEventLoop());
	Condition		_condition;

	public ChannelGroupFilter(Condition condition)
	{
		_condition = condition;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		_channels.add(ctx.channel());
		super.channelWritabilityChanged(ctx);
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
