package org.rzo.netty.ahessian.heartbeat;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Date;

import org.rzo.netty.ahessian.Constants;

public class ClientHeartbeatHandlerOutbound extends ChannelOutboundHandlerAdapter implements TimedOutAction
{
	HeartbeatHandlerInbound _handler;

	public ClientHeartbeatHandlerOutbound(HeartbeatHandlerInbound handler)
	{
		_handler = handler;
		_handler.setAction(this);
	}

	@Override
	public
	void timedOut(ChannelHandlerContext ctx)
	{
    	Constants.ahessianLogger.info("no writes since "+new Date(_handler.getLastCalled())+" -> send empty buffer heartbeat");
		ByteBuf b = ctx.alloc().buffer(1);
		b.writeByte(0);
        ChannelFuture f = ctx.writeAndFlush(b);
        try
		{
			f.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	_handler.ping();
        ctx.write(msg, promise);
    }
    


}
