package com.caucho.hessian4.io;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;

import java.io.IOException;


public interface FlushableOutput
{
	public void flush(ChannelPromise future)  throws IOException;
	public void reset();

}
