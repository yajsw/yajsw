package org.rzo.yajsw.nettyutils;

import io.netty.channel.ChannelHandlerContext;

public interface Condition
{
	public boolean isOk(ChannelHandlerContext ctx, Object msg);
}
