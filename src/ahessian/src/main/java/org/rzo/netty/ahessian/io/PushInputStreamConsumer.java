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
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.utils.MyReentrantLock;

public class PushInputStreamConsumer extends ChannelInboundHandlerAdapter
{

	volatile Lock _lock = new MyReentrantLock();
	AtomicInteger _consumerThreadsCount = new AtomicInteger(0);

	volatile InputStreamConsumer _consumer;
	volatile Executor _executor;

	public PushInputStreamConsumer(InputStreamConsumer consumer,
			Executor executor)
	{
		_consumer = consumer;
		_executor = executor;
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg)
			throws Exception
	{
		// input stream is consumed within a separate thread
		// we return the current worker thread to netty, so that it may continue
		// feeding the input stream
		_executor.execute(new Runnable()
		{
			public void run()
			{
				String tName = Thread.currentThread().getName();
				try
				{
					PushInputStreamConsumer.this.run(ctx, msg);
				}
				finally
				{
					Thread.currentThread().setName(tName);
					_consumerThreadsCount.decrementAndGet();
				}
			}
		});

	}

	private void run(ChannelHandlerContext ctx, Object evt)
	{
		if (_consumer.isBufferEmpty())
		{
			// we have nothing to consume
			return;
		}

		if (_consumerThreadsCount.incrementAndGet() > 2)
		{
			// there is already a thread consuming and another at the gate to
			// consume the last chunk
			_consumerThreadsCount.decrementAndGet();
			return;
		}

		Thread.currentThread().setName(
				"ahessian-PushInputStreamConsumer-#"
						+ _consumerThreadsCount.get());

		// consume only with one thread at a time
		_lock.lock();
		try
		{
			_consumer.consume(ctx, (InputStream) evt);
		}
		catch (Exception ex)
		{
			Constants.ahessianLogger.warn("", ex);
		}
		finally
		{
			_consumerThreadsCount.decrementAndGet();
			_lock.unlock();
		}

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		_lock.lock();
		try
		{
			_consumer.setContext(ctx);
		}
		finally
		{
			_lock.unlock();
		}
		ctx.fireChannelActive();
	}

}
