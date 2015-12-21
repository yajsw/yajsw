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
package org.rzo.netty.ahessian.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.Date;

import org.rzo.netty.ahessian.Constants;

public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter
		implements TimedOutAction
{
	static final ReadTimeoutException EXCEPTION = ReadTimeoutException.INSTANCE;

	HeartbeatHandlerInbound _handler;

	public ServerHeartbeatHandler(HeartbeatHandlerInbound handler)
	{
		_handler = handler;
		_handler.setAction(this);
	}

	@Override
	public void timedOut(ChannelHandlerContext ctx)
	{
		Constants.ahessianLogger.info("no reads since "
				+ new Date(_handler.getLastCalled()) + " -> close channel");
		ctx.channel().close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
		_handler.ping();
		ctx.fireChannelRead(msg);
	}

}
