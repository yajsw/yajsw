package org.rzo.yajsw.nettyutils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConditionFilter extends ChannelInboundHandlerAdapter
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
