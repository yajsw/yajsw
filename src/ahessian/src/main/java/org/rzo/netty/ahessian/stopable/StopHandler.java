/*******************************************************************************
 * Copyright  2015 rzorzorzo@users.sf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.rzo.netty.ahessian.stopable;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutor;

import java.util.Iterator;
import java.util.Map.Entry;

public class StopHandler extends ChannelInboundHandlerAdapter
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
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception
	{
		ChannelPipeline p = ctx.pipeline();
		Iterator<Entry<String, ChannelHandler>> it = p.iterator();
		while (it.hasNext())
		{
			Entry<String, ChannelHandler> e = it.next();
			if (e.getValue() instanceof StopableHandler)
			{
				StopableHandler h = (StopableHandler) e.getValue();
				if (h.isStopEnabled())
					h.stop();
			}

		}
		if (_executor != null && !_executor.isShuttingDown()
				&& !_executor.isTerminated())
			_executor.shutdownGracefully();
	}

}
