package org.rzo.netty.ahessian.heartbeat;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.Date;

import org.rzo.netty.ahessian.Constants;

public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter implements TimedOutAction
{
    static final ReadTimeoutException EXCEPTION = ReadTimeoutException.INSTANCE;


	HeartbeatHandlerInbound _handler;

	public ServerHeartbeatHandler(HeartbeatHandlerInbound handler)
	{
		_handler = handler;
		_handler.setAction(this);
	}
	@Override
	public
	void timedOut(ChannelHandlerContext ctx)
	{		
    	Constants.ahessianLogger.info("no reads since "+new Date(_handler.getLastCalled())+" -> close channel");
	    ctx.channel().close();
	 }
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
    	_handler.ping();
        ctx.fireChannelRead(msg);
    }



}
