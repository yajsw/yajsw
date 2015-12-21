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
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.rzo.netty.ahessian.rpc.message.GroupedMessage;
import org.rzo.netty.ahessian.stopable.StopableHandler;
import org.rzo.netty.ahessian.utils.MyReentrantLock;
import org.rzo.netty.ahessian.utils.TimedBlockingPriorityQueue;

abstract public class OutputProducer extends ChannelOutboundHandlerAdapter
		implements StopableHandler, ChannelInboundHandler
{

	class MessageEvent
	{
		Object _msg;
		ChannelPromise _future;

		public MessageEvent(Object msg, ChannelPromise future)
		{
			_msg = msg;
			_future = future;
		}

		public Object getMsg()
		{
			return _msg;
		}

		public ChannelPromise getFuture()
		{
			return _future;
		}
	}

	private TimedBlockingPriorityQueue<MessageEvent> _pendingCalls = new TimedBlockingPriorityQueue(
			"OutputProducer");

	AtomicInteger _producerThreadsCount = new AtomicInteger(0);
	Lock _lock = new MyReentrantLock();

	Executor _executor;
	Timer _timer;
	List<MessageEvent> _pendingTermination = new ArrayList<MessageEvent>();
	ChannelHandlerContext _ctx;

	volatile boolean _stop = false;

	public OutputProducer(Executor executor)
	{
		_executor = executor;
	}

	public void write(final ChannelHandlerContext ctx, Object e,
			ChannelPromise promise) throws Exception
	{
		// System.out.println(Thread.currentThread()+
		// " OutputProducer writeRequesed "+promise);
		if (e instanceof GroupedMessage)
		{
			GroupedMessage m = (GroupedMessage) e;
			_pendingCalls.put(new MessageEvent(e, promise), m.getGroup());
		}
		else
			_pendingCalls.put(new MessageEvent(e, promise));
		// System.out.println(System.currentTimeMillis() +
		// " "+"output producer added task");
		if (_producerThreadsCount.get() < 2)
			_executor.execute(new Runnable()
			{

				public void run()
				{
					produce(ctx);
				}

			});

	}

	@Override
	public void connect(final ChannelHandlerContext ctx,
			SocketAddress remoteAddress, SocketAddress localAddress,
			ChannelPromise promise) throws Exception
	{
		doChannelActive(ctx);
		super.connect(ctx, remoteAddress, localAddress, promise);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		doChannelActive(ctx);
		ctx.fireChannelActive();
	}

	private void doChannelActive(final ChannelHandlerContext ctx)
	{
		_ctx = ctx;
		_executor.execute(new Runnable()
		{
			public void run()
			{
				for (Iterator it = _pendingTermination.iterator(); it.hasNext();)
				{
					_lock.lock();
					try
					{
						if (_stop)
							return;
						MessageEvent e = (MessageEvent) it.next();
						if (e.getMsg() instanceof GroupedMessage)
						{
							GroupedMessage m = (GroupedMessage) e.getMsg();
							_pendingCalls.put(e, m.getGroup());
						}
						else
							_pendingCalls.put(e);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
					finally
					{
						_lock.unlock();
					}
				}
				produce(ctx);
			}
		});
	}

	private void produce(ChannelHandlerContext ctx)
	{
		if (_stop)
			return;

		if (_producerThreadsCount.incrementAndGet() > 2)
		{
			// there is already a thread consuming and another at the gate to
			// consume the last chunk
			_producerThreadsCount.decrementAndGet();
			return;
		}
		// System.out.println(Thread.currentThread()+" produce");
		boolean produced = false;
		_lock.lock();
		try
		{
			MessageEvent toSend = null;
			while (ctx.channel().isActive() && _pendingCalls.size() > 0)
			{
				if (_stop)
					return;

				try
				{
					toSend = _pendingCalls.take();
					// System.out.println(System.currentTimeMillis() +
					// " "+Thread.currentThread()+
					// " OutputProducer sendMessage "+toSend.getMsg());
					produceOutput(ctx, toSend.getMsg(), toSend.getFuture());
					_pendingTermination.add(toSend);
					produced = true;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					_pendingCalls.put(toSend,
							((GroupedMessage) toSend.getMsg()).getGroup());
				}
			}
			if (produced && _pendingCalls.size() == 0)
			{
				// System.out.println(System.currentTimeMillis() +
				// " "+Thread.currentThread()+ " OutputProducer flush");
				flashOutput(ctx);
				for (Iterator it = _pendingTermination.iterator(); it.hasNext();)
				{
					if (_stop)
						return;

					try
					{
						MessageEvent e = (MessageEvent) it.next();
						// GroupedMessage m = (GroupedMessage) e.getMsg();
						it.remove();
						e.getFuture().setSuccess();
						// System.out.println("set success "+e.getFuture());
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			_producerThreadsCount.decrementAndGet();
			_lock.unlock();
		}

	}

	protected abstract void flashOutput(ChannelHandlerContext ctx)
			throws Exception;

	protected abstract void produceOutput(ChannelHandlerContext ctx,
			Object msg, ChannelPromise future) throws Exception;

	public boolean isStopEnabled()
	{
		return true;
	}

	public void setStopEnabled(boolean stopEnabled)
	{
	}

	public void stop()
	{
		_stop = true;
		for (MessageEvent event : _pendingCalls)
		{
			event.getFuture().cancel(true);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception
	{
		ctx.fireChannelRead(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelReadComplete();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception
	{
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception
	{
		ctx.fireChannelWritabilityChanged();
	}

}
