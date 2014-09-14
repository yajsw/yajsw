package org.rzo.netty.ahessian.io;

import io.netty.channel.ChannelHandlerContext;

import java.io.InputStream;

public interface InputStreamConsumer
{
	public void consume(ChannelHandlerContext ctx, InputStream message);

	public boolean isBufferEmpty();
	
	public void setContext(ChannelHandlerContext ctx);
	
}
