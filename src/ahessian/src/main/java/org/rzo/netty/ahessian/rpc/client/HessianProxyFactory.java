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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.io.Hessian2Input;
import org.rzo.netty.ahessian.rpc.io.Hessian2Output;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyMessage;
import org.rzo.netty.ahessian.rpc.stream.ClientStreamManager;
import org.rzo.netty.ahessian.rpc.stream.InputStreamReplyMessage;
import org.rzo.netty.ahessian.session.ClientSessionFilter;
import org.rzo.netty.ahessian.utils.MyBlockingQueue;
import org.rzo.netty.ahessian.utils.MyLinkedBlockingQueue;
import org.rzo.netty.ahessian.utils.MyReentrantLock;
import org.rzo.netty.ahessian.utils.TimedBlockingPriorityQueue;

import com.caucho.hessian4.io.AbstractHessianInput;
import com.caucho.hessian4.io.AbstractHessianOutput;
import com.caucho.hessian4.io.HessianRemoteObject;

/**
 * Handles client side hessian rpc proxy invocations and is a factory for
 * service proxies. <br>
 * A typical setup for a protocol in a TCP/IP socket would be: <br>
 * 
 * <pre>
 * Executor executor = ...
 * HessianProxyFactory proxyFactory = ...
 * 
 * {@link ChannelPipeline} pipeline = ...;
 * pipeline.addLast(&quot;inputStream&quot;, new InputStreamDecoder(_executor));
 * pipeline.addLast(&quot;outputStream&quot;, new OutputStreamEncoder());        
 * pipeline.addLast(&quot;hessianReplyDecoder&quot;, new HessianRPCReplyDecoder(_factory));
 * pipeline.addLast(&quot;hessianCallEncoder&quot;, new HessianRPCCallEncoder());
 * pipeline.addLast(&quot;hessianHandler&quot;, proxyFactory);
 * </pre>
 * 
 * <br>
 * Typical usage within the client would be:
 * 
 * <pre>
 * 
 * ClientBootstrap bootstrap = ...
 * ChannelPipelineFactory pipelinetFactory = new ...(proxyFactory)
 * bootstrap.setPipelineFactory(...)
 * bootstrap.connect(...)
 * 
 * // get a service proxy 
 * Map options = new HashMap();
 * options.put(&quot;id&quot;, &quot;myServiceName&quot;);
 * // AsynchMyServiceInterface is an interface including the same methods as MyServiceInterface 
 * //except that the return type is always of type HessianProxyFuture
 * AsynchMyServiceInterface service = (AsynchMyServiceInterface) factory.create(AsynchMyServiceInterface.class, getClassLoader(), options);
 * 
 * // invoke a service method
 * HessianProxyFuture future = service.myMethod();
 * // wait for the result
 * // if an exception is thrown by the server the exception is thrown by the call to the get() method 
 * Object result = future.get();
 * </pre>
 */
@Sharable
public class HessianProxyFactory extends ChannelInboundHandlerAdapter implements
		Constants
{
	private static final int MAX_OPEN_CALLS = 50000;
	private volatile Map<Long, Future<Object>> _openCalls = Collections
			.synchronizedMap(new HashMap<Long, Future<Object>>());
	private volatile int _id = 0;
	private volatile Channel _channel = null;
	private volatile com.caucho.hessian4.client.HessianProxyFactory _factory = null;
	private volatile MyBlockingQueue<HessianRPCCallMessage> _pendingCalls;

	/** The _done listener. */
	Runnable _doneListener;

	/** The _executor. */
	Executor _executor;
	private Lock _lock = new MyReentrantLock();
	private Condition _connected = _lock.newCondition();

	/** The _stop. */
	boolean _stop = false;
	private String _name;
	private boolean _sessionListenerAdded = false;
	volatile private Runnable _closedSessionListener;
	volatile private Runnable _newSessionListener;
	volatile private Runnable _disconnectedListener;
	volatile private Runnable _connectedListener;

	Map<Object, InvocationHandler> _proxies = Collections
			.synchronizedMap(new HashMap<Object, InvocationHandler>());

	Timer _timer = new HashedWheelTimer();

	private volatile boolean _blocked = false;

	ClientStreamManager _clientStreamManager;

	/**
	 * Instantiates a new hessian proxy factory.
	 * 
	 * @param executor
	 *            the executor
	 */
	public HessianProxyFactory(Executor executor, String name)
	{
		this(executor, name, null, new HashMap());
	}

	public HessianProxyFactory(Executor executor, String name, Map options)
	{
		this(executor, name, null, options);
	}

	public HessianProxyFactory(Executor executor, String name,
			ClassLoader loader, Map options)
	{
		_executor = executor;
		_name = name;
		if (options != null)
			_pendingCalls = new TimedBlockingPriorityQueue<HessianRPCCallMessage>(
					options, null, "HessianProxyFactory-PendingCalls");
		else
			_pendingCalls = new MyLinkedBlockingQueue();
		if (loader == null)
			_factory = new com.caucho.hessian4.client.HessianProxyFactory();
		else
			_factory = new com.caucho.hessian4.client.HessianProxyFactory(
					loader);
		/*
		 * _executor.execute(new Runnable() { public void run() {
		 * Thread.currentThread().setName("HessianProxyFactory-Call-Tx");
		 * HessianRPCCallMessage message = null; while (!_stop) { // if previous
		 * message sent if (message == null) try { message =
		 * _pendingCalls.take(); } catch (InterruptedException e1) {
		 * Constants.ahessianLogger.warn("", e1); } if (message == null)
		 * continue; _lock.lock(); Channel channel = getChannel(); while
		 * (channel == null || !channel.isConnected() && !_stop) try {
		 * _connected.await(1000, TimeUnit.MILLISECONDS); channel =
		 * getChannel(); } catch (InterruptedException e) {
		 * Constants.ahessianLogger.warn("", e); } _lock.unlock(); if (!_stop &&
		 * message != null && message.getMethod() != null) try { ChannelFuture
		 * future = channel.write(message); future.await(); if
		 * (future.isSuccess()) { // if (_pendingCalls.size() == 0) //
		 * channel.write(new Integer(0)); message = null; } else
		 * ahessianLogger.warn("cannot send message, will retry"); } catch
		 * (Exception ex) { Constants.ahessianLogger.warn("", ex); } else if
		 * (message.getMethod() == null) message = null;
		 * 
		 * }
		 * 
		 * } });
		 */
	}

	/**
	 * Gets the hessian2 input.
	 * 
	 * @param is
	 *            the is
	 * 
	 * @return the hessian2 input
	 */
	public AbstractHessianInput getHessian2Input(InputStream is)
	{
		return new Hessian2Input(is);
	}

	public AbstractHessianOutput getHessian2Output(OutputStream out)
	{
		Hessian2Output out2 = new Hessian2Output(out);
		out2.setSerializerFactory(_factory.getSerializerFactory());
		return out2;
	}

	/**
	 * Checks if is overload enabled.
	 * 
	 * @return true, if is overload enabled
	 */
	public boolean isOverloadEnabled()
	{
		return _factory.isOverloadEnabled();
	}

	/**
	 * Send request.
	 * 
	 * @param methodName
	 *            the method name
	 * @param args
	 *            the args
	 * @param options
	 *            the options
	 * 
	 * @return the future< object>
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	synchronized Future<Object> sendRequest(String methodName, Object[] args,
			Map options) throws InterruptedException
	{
		long t = System.currentTimeMillis();
		if (_blocked)
			throw new RuntimeException("send blocked");
		if (_stop)
			return null;
		Map<Object, Object> headers = options;
		final Long id = new Long(_id);
		_id++;
		headers.put(CALL_ID_HEADER_KEY, id);
		final HessianProxyFuture future = new HessianProxyFuture();
		future.handleCallbacks(args);
		final HessianRPCCallMessage message = new HessianRPCCallMessage(
				methodName, args, headers, null);
		int i = 0;
		while (_openCalls.size() > MAX_OPEN_CALLS && getChannel() != null)
		{
			// System.out.println("too many open calls -> wait "+i++);
			Thread.sleep(10);
		}
		_openCalls.put(id, future);
		Integer g = (Integer) options.get("group");
		final Integer group = g == null ? 0 : g;
		long timeout = _pendingCalls.getTimeout(group);
		if (timeout > 0)
		{
			TimerTask task = new TimerTask()
			{

				public void run(Timeout arg0) throws Exception
				{
					_openCalls.remove(id);
					future.timedOut();
				}

			};
			future.setTimeout(_timer.newTimeout(task, timeout,
					TimeUnit.MILLISECONDS));
		}
		Channel channel = null;
		/*
		 * while ((channel = getChannel()) == null) { _lock.lock(); try {
		 * _connected.await(100, TimeUnit.MILLISECONDS); } finally {
		 * _lock.unlock(); } }
		 */
		channel = getChannel();
		if (channel == null)
			throw new RuntimeException("channel closed");
		while (!channel.isWritable() && channel.isActive())
		{
			// System.out.println("channel wait call "+Thread.currentThread().getName());
			Thread.sleep(100);
		}
		channel.write(message);
		// System.out.println("sendRequest "+(System.currentTimeMillis()-t));

		return future;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss
	 * .netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		if (e instanceof HessianRPCReplyMessage)
		{
			final HessianRPCReplyMessage message = (HessianRPCReplyMessage) e;
			{
				final Long id = message.getCallId();
				if (id != null)
				{
					final HessianProxyFuture future = (HessianProxyFuture) _openCalls
							.get(id);
					if (future == null)
					{
						ahessianLogger
								.warn("no future found for call-id " + id);
						return;
					}
					if (message.getCompleted() == null
							|| Boolean.TRUE.equals(message.getCompleted()))
						if ((!future.hasCallbacks()))
						{
							_openCalls.remove(id);
						}
					if (_doneListener != null && _openCalls.isEmpty())
						_doneListener.run();
					if (future != null)
						// setting message in future may fire listeners -> run
						// in separate thread
						_executor.execute(new Runnable()
						{
							public void run()
							{
								// System.out.println("executing callback");
								try
								{
									if (message.getValue() instanceof InputStreamReplyMessage)
									{
										InputStream stream = _clientStreamManager
												.newInputStream(((InputStreamReplyMessage) message
														.getValue()).getId());
										// caller should get a stream, not the
										// reply
										message.setValue(stream);
									}
									future.set(message, ctx);
									// check in case this was a callback
									if (future.isDone())
										if (!future.hasCallbacks())
										{
											_openCalls.remove(id);

										}
								}
								catch (Throwable e)
								{
									e.printStackTrace();
								}
							}
						});
					else
						ahessianLogger.warn("no future for call reply " + id
								+ " " + message.getValue());
				}
				else
					ahessianLogger.warn("message missing id " + message);

			}
		}
		else if (e instanceof InputStreamReplyMessage)
		{
			_clientStreamManager.messageReceived((InputStreamReplyMessage) e);
		}
		// ctx.fireChannelRead(e);
		ctx.fireChannelReadComplete();
	}

	/**
	 * Creates a service proxy.
	 * 
	 * @param api
	 *            the "asynched" api of the service
	 * @param loader
	 *            the class loader for creating the proxy
	 * @param options
	 *            the options
	 * 
	 * @return the object
	 */
	public Object create(Class api, ClassLoader loader, Map options)
	{
		if (api == null)
			throw new NullPointerException(
					"api must not be null for HessianProxyFactory.create()");
		InvocationHandler handler = null;
		if (options == null)
			options = new HashMap();
		handler = new AsyncHessianProxy(this, api, options);
		if (options.get("sync") != null)
			handler = new SyncHessianProxy(handler);

		Object result = Proxy.newProxyInstance(loader, new Class[] { api,
				HessianRemoteObject.class }, handler);
		_proxies.put(result, handler);
		return result;

	}

	public void returnProxy(Object proxy)
	{
		Object handler = _proxies.remove(proxy);
		if (handler != null)
			if (handler instanceof SyncHessianProxy)
				handler = ((SyncHessianProxy) handler)._handler;
		((AsyncHessianProxy) handler).invalidate();
	}

	/**
	 * Gets the channel.
	 * 
	 * @return the channel
	 */
	public Channel getChannel()
	{
		if (_channel == null || !_channel.isActive() || !_channel.isWritable())
			return null;
		return _channel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss
	 * .netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		_channel = ctx.channel();
		if (_connectedListener != null)
			_executor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						_connectedListener.run();
					}
					catch (Throwable ex)
					{
						Constants.ahessianLogger.warn("", ex);
					}
				}
			});
		_lock.lock();
		try
		{
			if (!_sessionListenerAdded)
			{
				if (ctx.pipeline().get(ClientSessionFilter.class) != null)
				{
					ClientSessionFilter sessionHandler = (ClientSessionFilter) ctx
							.pipeline().get(ClientSessionFilter.class);
					sessionHandler.addSessionClosedListener(new Runnable()
					{
						public void run()
						{
							_lock.lock();
							try
							{
								invalidateProxies();
								_openCalls.clear();

								_pendingCalls.clear();
							}
							finally
							{
								_lock.unlock();
							}
							if (_closedSessionListener != null)
								try
								{
									_closedSessionListener.run();
								}
								catch (Throwable ex)
								{
									Constants.ahessianLogger.warn("", ex);
								}
						}
					});
					sessionHandler.addSessionNewListener(new Runnable()
					{
						public void run()
						{
							if (_newSessionListener != null)
								try
								{
									_newSessionListener.run();
								}
								catch (Throwable ex)
								{
									Constants.ahessianLogger.warn("", ex);
								}
						}
					});
					_sessionListenerAdded = true;
				}
			}
		}
		finally
		{
			_connected.signal();
			_lock.unlock();
			super.channelActive(ctx);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.
	 * jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ChannelStateEvent)
	 */
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		_channel = null;
		// _stop = true;
		_lock.lock();
		try
		{
			_connected.signal();
		}
		finally
		{
			_lock.unlock();
		}
		// put something in the queue in case the worker thread hangs in
		// _pendingCalls.take()
		_pendingCalls.offer(new HessianRPCCallMessage(null, null, null, null));
		if (_disconnectedListener != null)
			_executor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						_disconnectedListener.run();
					}
					catch (Throwable ex)
					{
						Constants.ahessianLogger.warn("", ex);
					}
				}
			});

		super.channelInactive(ctx);

	}

	/**
	 * Sets the done listener. This listener is fired whenever all requests have
	 * been completed
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void setDoneListener(Runnable listener)
	{
		_doneListener = listener;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
	{
		ahessianLogger.warn("error accessing service " + _name + " Exception "
				+ e.getClass() + " " + e.getCause().getMessage());
		ctx.channel().disconnect();
		ctx.channel().close();
		if (!_stop)
		{
			_channel = null;
			// _stop = true;
			_lock.lock();
			try
			{
				_connected.signal();
			}
			finally
			{
				_lock.unlock();
			}
			// put something in the queue in case the worker thread hangs in
			// _pendingCalls.take()
			_pendingCalls.offer(new HessianRPCCallMessage(null, null, null,
					null));
		}
	}

	public void invalidateProxies()
	{
		for (Object proxy : new HashSet(_proxies.keySet()))
		{
			returnProxy(proxy);
		}
	}

	public void setClosedSessionListener(Runnable listener)
	{
		_closedSessionListener = listener;
	}

	public void setDisconnectedListener(Runnable listener)
	{
		_disconnectedListener = listener;
	}

	public void setConnectedListener(Runnable listener)
	{
		_connectedListener = listener;
	}

	public void setNewSessionListener(Runnable listener)
	{
		_newSessionListener = listener;
	}

	public void invalidateAllPendingCalls()
	{

		final HessianRPCReplyMessage message = new HessianRPCReplyMessage(null,
				new RuntimeException("connection closed"), null);
		for (Future future : new ArrayList<Future>(_openCalls.values()))
		{
			((HessianProxyFuture) future).set(message);
		}
		_openCalls.clear();
		_pendingCalls.clear();
	}

	public void setBlocked(boolean blocked)
	{
		_blocked = blocked;
	}

}
