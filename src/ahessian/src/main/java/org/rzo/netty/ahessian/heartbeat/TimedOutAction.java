package org.rzo.netty.ahessian.heartbeat;

import io.netty.channel.ChannelHandlerContext;

public interface TimedOutAction
{
	void timedOut(ChannelHandlerContext ctx);
}
