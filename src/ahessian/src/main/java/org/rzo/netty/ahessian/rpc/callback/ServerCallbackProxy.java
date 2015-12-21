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
package org.rzo.netty.ahessian.rpc.callback;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.client.HessianProxyFuture;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler;

public class ServerCallbackProxy implements InvocationHandler, Constants
{
	private boolean _done = false;
	private HessianRPCServiceHandler _handler;
	private HessianRPCCallMessage _message;
	private ClientCallback _clientCallback;
	boolean _closed = false;
	private Set<String> _returnMethods;
	static private AtomicLong _idCounter = new AtomicLong();
	static private Map<Long, HessianProxyFuture> _openCallbackCalls = new ConcurrentHashMap<Long, HessianProxyFuture>();
	static private Timer _timer = new HashedWheelTimer();

	public ServerCallbackProxy(HessianRPCServiceHandler handler,
			HessianRPCCallMessage message, ClientCallback clientCallback)
	{
		_message = message;
		_clientCallback = clientCallback;
		_handler = handler;
		_returnMethods = new HashSet();
		if (clientCallback.getReturnMethods() != null)
			for (String method : clientCallback.getReturnMethods())
				_returnMethods.add(method);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{
		return invoke(method.getName(), args);
	}

	public Object invoke(String method, Object[] args)
	{
		if (_closed)
			throw new RuntimeException(
					"cannot invoke callback after call to setDone(true)");
		String methodName = method;
		if ("setDone".equals(methodName) && args.length == 1
				&& (args[0] instanceof Boolean))
		{
			_done = ((Boolean) args[0]).booleanValue();
			return null;
		}
		if ("isDone".equals(method) && (args == null || args.length == 0))
		{
			return (Boolean) _done;
		}

		if ("isValid".equals(method) && (args == null || args.length == 0))
		{
			return (Boolean) _message.isValid();
		}

		if ("toString".equals(method) && (args == null || args.length == 0))
		{
			return this.toString();
		}

		if ("hashCode".equals(method) && (args == null || args.length == 0))
		{
			return this.hashCode();
		}

		if ("equals".equals(method) && (args.length == 1))
		{
			return this.equals(args[0]);
		}

		CallbackReplyMessage reply = new CallbackReplyMessage(methodName, args,
				null, _message);
		reply.setCallId((Long) _message.getHeaders().get(CALL_ID_HEADER_KEY));
		reply.setGroup((Integer) _message.getHeaders().get(GROUP_HEADER_KEY));
		reply.setCallbackId(_clientCallback.getId());
		reply.setCallbackArgs(args);
		reply.setCallbackMethod(methodName);
		reply.setCallbackCallId(_idCounter.incrementAndGet());
		if (_done)
			reply.setCallbackDone(true);

		HessianProxyFuture future = null;
		if (_returnMethods.contains(methodName))
		{
			future = new HessianProxyFuture();
			// System.out.println("put _openCallbackCalls "+
			// reply.getCallbackCallId() + " "+future);
			_openCallbackCalls.put(reply.getCallbackCallId(), future);
		}

		_handler.writeResult(reply);

		if (_done)
			_closed = true;

		Object result = null;
		if (_returnMethods.contains(methodName))
			result = waitForCallbackResult(reply.getCallbackCallId(), future);
		return result;
	}

	private Object waitForCallbackResult(final Long id,
			final HessianProxyFuture future)
	{
		long timeout = 10000;
		if (timeout > 0)
		{
			TimerTask task = new TimerTask()
			{

				public void run(Timeout arg0) throws Exception
				{
					_openCallbackCalls.remove(id);
					future.timedOut();
				}

			};
			future.setTimeout(_timer.newTimeout(task, timeout,
					TimeUnit.MILLISECONDS));
		}

		try
		{
			return future.getCallbackResult();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void setCallbackResult(HessianRPCCallMessage reply)
	{
		Long id = (Long) reply.getHeaders().get(CALLBACK_CALL_ID_HEADER_KEY);
		if (id == null)
		{
			return;
		}

		HessianProxyFuture future = _openCallbackCalls.get(id);
		// System.out.println("get _openCallbackCalls "+ id + " "+future);
		if (future == null)
			return;
		future.setCallbackResult(reply);
	}

}
