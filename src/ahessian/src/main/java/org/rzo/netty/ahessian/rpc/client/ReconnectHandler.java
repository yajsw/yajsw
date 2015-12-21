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
package org.rzo.netty.ahessian.rpc.client;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import org.rzo.netty.ahessian.Constants;

public class ReconnectHandler extends ChannelInboundHandlerAdapter
{
	private Timer _timer;
	private long RECONNECT_DELAY = 10000;
	private long MAX_RECONNECT_DELAY = 10000;
	private BootstrapProvider _bootstrap;
	private volatile boolean _stop = false;
	private volatile Timeout _timeout;
	private volatile int _retryCounter = 0;

	public ReconnectHandler(BootstrapProvider bootstrap, long reconnectDelay,
			Timer timer)
	{
		RECONNECT_DELAY = reconnectDelay;
		_bootstrap = bootstrap;
		_timer = timer;
	}

	public ReconnectHandler(BootstrapProvider bootstrap)
	{
		_bootstrap = bootstrap;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		ctx.fireChannelInactive();
		scheduleReconnect();
	}

	private synchronized void scheduleReconnect()
	{
		if (_stop)
			return;
		if (_timeout != null)
			return;
		Constants.ahessianLogger.warn("channel closed wait to reconnect ...");
		_retryCounter++;
		long retryIntervall = Math.min(RECONNECT_DELAY * _retryCounter,
				MAX_RECONNECT_DELAY);
		_timeout = _timer.newTimeout(new TimerTask()
		{
			public void run(Timeout timeout) throws Exception
			{
				_timeout = null;
				connect(_bootstrap.getBootstrap());

			}
		}, retryIntervall, TimeUnit.MILLISECONDS);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
	{
		// if we get an exception : close the channel
		Throwable cause = e;
		cause.printStackTrace();
		if (cause instanceof ConnectException)
		{
			Constants.ahessianLogger.warn("conection lost");
			scheduleReconnect();
		}
		try
		{
			ctx.channel().close();
		}
		catch (Exception ex)
		{

		}
	}

	public void stop()
	{
		_stop = true;
		Timeout timeout = _timeout;
		_timeout = null;
		timeout.cancel();
	}

	protected void connect(AbstractBootstrap bootstrap)
	{
		Channel channel = null;
		Constants.ahessianLogger.warn("reconnecting...");
		while (channel == null && !_stop)
			try
			{
				channel = ((Bootstrap) bootstrap).connect().sync().channel();
			}
			catch (Exception ex)
			{
				if (ex instanceof ConnectException)
				{
					System.out.println(ex);
					try
					{
						Thread.sleep(RECONNECT_DELAY);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}

	}

}
