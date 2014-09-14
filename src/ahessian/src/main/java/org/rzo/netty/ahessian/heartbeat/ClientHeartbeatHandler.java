package org.rzo.netty.ahessian.heartbeat;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.Timer;

import java.util.Date;

import org.rzo.netty.ahessian.Constants;

public class ClientHeartbeatHandler extends AbstractHeartbeatHandler
{

	public ClientHeartbeatHandler(String name, Timer timer, long timeout)
	{
		super(name, timer, timeout);
	}

	@Override
	void timedOut(ChannelHandlerContext ctx)
	{
    	Constants.ahessianLogger.info("no writes since "+new Date(getLastCalled())+" -> send empty buffer heartbeat");
		ByteBuf b = ctx.alloc().buffer(1);
		b.writeByte(0);
        ChannelFuture f = _ctx.writeAndFlush(b);
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
    	ping();
        ctx.write(msg, promise);
    }
    


}
