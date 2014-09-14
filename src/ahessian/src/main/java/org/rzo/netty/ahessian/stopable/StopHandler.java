package org.rzo.netty.ahessian.stopable;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutor;

import java.util.Iterator;
import java.util.Map.Entry;


public class StopHandler extends ChannelHandlerAdapter
{
	
	EventExecutor _executor;
	
	public StopHandler(EventExecutor executor)
	{
		_executor = executor;
	}

	public StopHandler()
	{
		_executor = null;
	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) throws Exception
	{
		ChannelPipeline p = ctx.pipeline();
		Iterator<Entry<String, ChannelHandler>> it = p.iterator();
		while (it.hasNext())
		{
			Entry<String, ChannelHandler> e = it.next();
			if (e.getValue() instanceof StopableHandler)
			{
				StopableHandler h = (StopableHandler)e.getValue();
				if (h.isStopEnabled())
					h.stop();
			}
				
		}
		if (_executor != null && !_executor.isShuttingDown() && !_executor.isTerminated())
			_executor.shutdownGracefully();
	}


}
