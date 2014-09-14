package org.rzo.yajsw.nettyutils;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ConditionFilter extends ChannelHandlerAdapter
{

	Condition	_condition;

	public ConditionFilter(Condition condition)
	{
		_condition = condition;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		if (_condition.isOk(ctx, null))
		{
			// forward if condtion met
			ctx.fireChannelActive();
		}
		else
		{
			ctx.channel().close();
		}
	}

}
