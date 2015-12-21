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
package org.rzo.yajsw.nettyutils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.SocketAddress;

public class ChannelGroupFilter extends ChannelOutboundHandlerAdapter
{
	ChannelGroup _channels = new DefaultChannelGroup(
			GlobalEventExecutor.INSTANCE);
	Condition _condition;

	public ChannelGroupFilter(Condition condition)
	{
		_condition = condition;
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception
	{
		_channels.add(ctx.channel());
		ctx.fireChannelWritabilityChanged();
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception
	{
		if (_condition.isOk(ctx, msg))
		{
			_channels.remove(ctx.channel());
			_channels.close();
		}
		ctx.write(msg, promise);
	}

}
