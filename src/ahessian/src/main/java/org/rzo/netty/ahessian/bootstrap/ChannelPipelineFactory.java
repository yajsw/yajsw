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
package org.rzo.netty.ahessian.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.LinkedList;
import java.util.List;

import org.rzo.netty.ahessian.log.OutLogger;

public abstract class ChannelPipelineFactory<C extends Channel> extends
		ChannelInitializer<C>
{
	private volatile boolean _debug = false;
	EventExecutorGroup _group;

	public ChannelPipelineFactory(EventExecutorGroup executor)
	{
		_group = executor;
	}

	public ChannelPipelineFactory()
	{
	}

	public EventExecutorGroup getGroup()
	{
		return _group;
	}

	public ChannelPipelineFactory<C> debug()
	{
		_debug = true;
		return this;
	}

	public static HandlerList handlerList(ChannelHandler... handlers)
	{
		HandlerList result = new HandlerList();
		int i = 0;
		for (ChannelHandler handler : handlers)
		{
			result.addLast(handler.getClass().getSimpleName() + "_" + i,
					handler);
		}
		return result;
	}

	@SuppressWarnings("serial")
	public static class HandlerList extends LinkedList<HandlerEntry>
	{
		Channel _channel;

		public void addLast(String name, ChannelHandler handler)
		{
			super.addLast(new HandlerEntry(name, handler));
		}

		public void addLast(String name, ChannelHandler handler,
				EventExecutorGroup group)
		{
			super.addLast(new HandlerEntry(name, handler, group));
		}

		public void mixin(ChannelHandlerContext ctx)
		{
			ChannelPipeline pipeline = ctx.pipeline();
			for (HandlerEntry entry : this)
			{
				pipeline.addLast(entry.getKey(), entry.getValue());
			}
			_channel = ctx.channel();

		}

		public boolean hasChannel()
		{
			return _channel != null && _channel.isActive();
		}

		public void close()
		{
			if (_channel != null)
				try
				{
					_channel.close().sync();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	public static class HandlerEntry
	{
		ChannelHandler _value;
		String _key;
		EventExecutorGroup _group;

		public HandlerEntry(String name, ChannelHandler handler)
		{
			this(name, handler, null);
		}

		public HandlerEntry(String name, ChannelHandler handler,
				EventExecutorGroup group)
		{
			_value = handler;
			_key = name;
			_group = group;
		}

		public String getKey()
		{
			return _key;
		}

		public ChannelHandler getValue()
		{
			return _value;
		}

		public EventExecutorGroup getGroup()
		{
			return _group;
		}

	}

	@Override
	protected void initChannel(C ch) throws Exception
	{
		if (_debug)
			ch.pipeline().addFirst("xlogger", new OutLogger("first"));

		List<HandlerEntry> list = getPipeline();
		for (HandlerEntry entry : list)
		{
			if (entry.getGroup() == null)
				ch.pipeline().addLast(entry.getKey(), entry.getValue());
			else
				ch.pipeline().addLast(entry.getGroup(), entry.getKey(),
						entry.getValue());
		}

		// System.out.println("added "+list.size()+" handlers to pipeline "+ch);
	}

	public abstract HandlerList getPipeline() throws Exception;

}
