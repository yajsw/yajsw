package org.rzo.netty.ahessian.heartbeat;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.Timer;

import java.util.Date;

import org.rzo.netty.ahessian.Constants;

public class ServerHeartbeatHandler extends AbstractHeartbeatHandler
{
    static final ReadTimeoutException EXCEPTION = ReadTimeoutException.INSTANCE;


	public ServerHeartbeatHandler(String name, Timer timer, long timeout)
	{
		super(name, timer, timeout);
	}

	@Override
	void timedOut(ChannelHandlerContext ctx)
	{		
    	Constants.ahessianLogger.info("no reads since "+new Date(getLastCalled())+" -> close channel");
	    ctx.channel().close();
	 }
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
    	ping();
        ctx.fireChannelRead(msg);
    }



}
