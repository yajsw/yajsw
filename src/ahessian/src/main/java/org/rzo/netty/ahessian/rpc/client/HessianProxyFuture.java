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

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Timeout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.callback.Callback;
import org.rzo.netty.ahessian.rpc.callback.CallbackReplyMessage;
import org.rzo.netty.ahessian.rpc.callback.ClientCallback;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyMessage;
import org.rzo.netty.ahessian.utils.MyReentrantLock;

/**
 * Future object returned when executing a remote method invocation. <br>
 * Note: within a continuation scenario, get() will return the last result
 * received. Results sent by the server will override the result available on
 * the client.
 */
public class HessianProxyFuture implements Future<Object>, Constants
{

	/** indicates if the invocation has been completed */
	private boolean _done = false;

	/** TODO indicates if the invocation has been canceled or timed out. */
	private boolean _canceled = false;

	/** result of the invocation. */
	private HessianRPCReplyMessage _result = null;

	private Lock _lock = new MyReentrantLock();
	private Condition _resultReceived = _lock.newCondition();
	private Collection<Runnable> _listeners = Collections
			.synchronizedCollection(new ArrayList<Runnable>());
	private volatile Map<Long, ClientCallback> _callbacks = Collections
			.synchronizedMap(new HashMap());
	private volatile Timeout _timeout = null;
	private volatile HessianRPCCallMessage _callbackResult;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */

	public boolean cancel(boolean mayInterruptIfRunning)
	{
		_canceled = true;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get()
	 */

	public Object get() throws InterruptedException, ExecutionException
	{
		Object result = null;
		_lock.lock();
		try
		{
			while (_result == null)
			{
				_resultReceived.await();
			}
			if (_result.getFault() != null)
				throw new ExecutionException(_result.getFault());
			else
				return _result.getValue();
		}
		finally
		{
			_lock.unlock();
		}
	}

	public Object getCallbackResult() throws InterruptedException,
			ExecutionException
	{
		_lock.lock();
		try
		{
			while (_callbackResult == null)
			{
				_resultReceived.await();
			}
			Throwable exception = null;
			Object result = null;
			Object[] args = _callbackResult.getArgs();
			if (args == null)
				return null;
			if (args.length == 0)
				return null;
			if (args.length >= 1)
				exception = (Throwable) args[0];
			if (args.length == 2)
				result = args[1];
			if (exception != null)
				throw new ExecutionException(exception);
			else
				return result;
		}
		finally
		{
			_lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */

	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException
	{
		Object result = null;
		_lock.lock();
		try
		{
			if (_result == null)
				_resultReceived.await(timeout, unit);
			if (_result == null)
				throw new TimeoutException();
			if (_result.getFault() != null)
				throw new ExecutionException(_result.getFault());
			else
				return _result.getValue();
		}
		finally
		{
			_lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isCancelled()
	 */

	public boolean isCancelled()
	{
		return _canceled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isDone()
	 */

	public boolean isDone()
	{
		return _done;
	}

	protected synchronized void set(HessianRPCReplyMessage message)
	{
		set(message, null);
	}

	public synchronized void set(HessianRPCReplyMessage message,
			ChannelHandlerContext ctx)
	{
		_lock.lock();
		if (_timeout != null)
			_timeout.cancel();
		try
		{
			if (message instanceof CallbackReplyMessage)
			{
				handleCallbackReply((CallbackReplyMessage) message, ctx);
			}
			else
			{
				_done = true;
				_result = message;
				_resultReceived.signal();
				callListners();
			}
		}
		finally
		{
			_lock.unlock();
		}
	}

	public synchronized void setCallbackResult(HessianRPCCallMessage result)
	{
		_lock.lock();
		if (_timeout != null)
			_timeout.cancel();
		try
		{
			_done = true;
			_callbackResult = result;
			_resultReceived.signal();
			callListners();
		}
		finally
		{
			_lock.unlock();
		}
	}

	private void handleCallbackReply(CallbackReplyMessage message,
			ChannelHandlerContext ctx)
	{
		Long callbackId = message.getCallbackId();
		if (callbackId == null)
			return;
		ClientCallback callback = _callbacks.get(callbackId);
		if (callback == null)
		{
			System.out.println("no callback found for " + callbackId);
			return;
		}
		callback.invoke(message, ctx);
		if (message.isDone())
		{
			_callbacks.remove(callbackId);
			// System.out.println("removed callback "+callbackId);
		}

	}

	private void callListners()
	{
		synchronized (_listeners)
		{
			for (Runnable listener : _listeners)
				listener.run();
		}
	}

	/**
	 * Adds the listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(Runnable listener)
	{
		_lock.lock();
		try
		{
			if (isDone())
				listener.run();
			else
				_listeners.add(listener);
		}
		finally
		{
			_lock.unlock();
		}
	}

	/**
	 * Removes the listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeListener(Runnable listener)
	{
		_listeners.remove(listener);
	}

	public void handleCallbacks(Object[] args)
	{
		if (args == null)
			return;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i] instanceof Callback)
			{
				ClientCallback c = new ClientCallback((Callback) args[i]);
				_callbacks.put(c.getId(), c);
				args[i] = c;
			}
		}

	}

	public boolean hasCallbacks()
	{
		return _callbacks.size() != 0;
	}

	public void setTimeout(Timeout timeout)
	{
		_timeout = timeout;
	}

	public void timedOut()
	{
		_lock.lock();
		_timeout = null;
		try
		{
			this.set(new HessianRPCReplyMessage(null, new TimeoutException(),
					null));
		}
		finally
		{
			_lock.unlock();
		}
	}

}
