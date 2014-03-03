package org.rzo.netty.ahessian.io;

import java.io.InputStream;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.caucho.hessian4.io.HessianProtocolException;

public interface InputStreamConsumer
{
	public void consume(ChannelHandlerContext ctx, InputStream message);

	public boolean isBufferEmpty();
	
	public void setContext(ChannelHandlerContext ctx);
	
}
