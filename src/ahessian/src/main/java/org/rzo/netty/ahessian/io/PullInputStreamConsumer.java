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
package org.rzo.netty.ahessian.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.stopable.StopableHandler;
import org.rzo.netty.ahessian.utils.MyReentrantLock;

public class PullInputStreamConsumer extends ChannelInboundHandlerAdapter
		implements StopableHandler
{
	final InputStreamConsumer _consumer;
	static ExecutorService _executor = Executors.newCachedThreadPool();
	final Lock _lock = new MyReentrantLock();
	final Condition _hasData = _lock.newCondition();
	volatile boolean _stop = false;
	volatile ChannelHandlerContext _ctx;
	volatile InputStream _inputStream;
	volatile boolean _waiting = false;
	static AtomicInteger _threadCounter = new AtomicInteger(0);
	private boolean _stopEnabled = true;
	volatile Thread _currentThread = null;

	public PullInputStreamConsumer(InputStreamConsumer consumer)
	{
		_consumer = consumer;
	}

	private void waitForData() throws Exception
	{
		// System.out.println("wait for data "+System.currentTimeMillis());
		while (!_stop
				&& (_ctx == null || !_ctx.channel().isActive()
						|| _inputStream == null || _inputStream.available() == 0))
		{

			_lock.lock();
			try
			{
				_waiting = true;
				_hasData.await(500, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				Constants.ahessianLogger.warn("", e);
			}
			finally
			{
				_waiting = false;
				_lock.unlock();
			}
		}
		// System.out.println("got data "+System.currentTimeMillis());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		// if (_executor == null)
		{
			// _executor = Executors.newSingleThreadExecutor();

			_executor.execute(new Runnable()
			{
				public void run()
				{
					String tName = Thread.currentThread().getName();
					Thread.currentThread().setName(
							"ahessian-PullInputStreamConsumer-#"
									+ _threadCounter.incrementAndGet());
					_currentThread = Thread.currentThread();
					try
					{
						waitForData();
						while (!_stop)
						{
							try
							{
								_consumer.consume(_ctx, _inputStream);
								// System.out.println("consumed "+System.currentTimeMillis());
							}
							catch (Exception ex)
							{
								ex.printStackTrace();
							}
							waitForData();
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
					finally
					{
						Thread.currentThread().setName(tName);
						// _threadCounter.decrementAndGet();
					}
				}
			});
		}

		if (_ctx != ctx)
			_ctx = ctx;
		InputStream in = InputStreamHandler.getInputStream(ctx);
		if (_inputStream != in)
		{
			_inputStream = in;
			((InputStreamBuffer) _inputStream).setReadTimeout(-1);
		}
		_consumer.setContext(ctx);
		if (_waiting)
		{
			_lock.lock();
			try
			{
				_hasData.signal();
			}
			finally
			{
				_lock.unlock();
			}
		}
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		_lock.lock();
		try
		{
			_consumer.setContext(null);
			_ctx = ctx;
		}
		finally
		{
			_lock.unlock();
		}
		ctx.fireChannelInactive();
	}

	public boolean isStopEnabled()
	{
		return _stopEnabled;
	}

	public void setStopEnabled(boolean stopEnabled)
	{
		_stopEnabled = stopEnabled;
	}

	public void stop()
	{
		_stop = true;
		// _executor.shutdown();
		_currentThread.interrupt();
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception
	{
		// System.out.println(System.currentTimeMillis()+" PullInputStream.channelRead +");

		_lock.lock();
		try
		{
			if (_waiting)
			{
				// System.out.println("signal "+System.currentTimeMillis());
				_hasData.signal();
			}
		}
		finally
		{
			_lock.unlock();
		}
		// System.out.println(System.currentTimeMillis()+" "+Thread.currentThread().getName()+" PullInputStream.channelRead -");

	}

}
