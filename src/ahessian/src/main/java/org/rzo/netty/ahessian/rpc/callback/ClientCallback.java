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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;

public class ClientCallback implements Serializable
{

	private transient Callback _callback;
	private Long _id;
	private String _callbackClass;
	private String[] _interfaces;
	private String[] _returnMethods;
	private static final AtomicLong _idCounter = new AtomicLong();
	private transient boolean _done = false;

	public ClientCallback()
	{

	}

	public ClientCallback(Callback callback)
	{
		_callback = callback;
		_id = _idCounter.getAndIncrement();
		_callbackClass = _callback.getClass().getName();
		// determine the interfaces on the client,
		// to avoid that the server needs to know the callback implementation
		_interfaces = determineInterfaces(_callback.getClass());
		_returnMethods = determineReturnMethods(_callback.getClass());
	}

	private String[] determineReturnMethods(Class<? extends Callback> clazz)
	{
		List<Method> methods = new ArrayList();
		for (Method method : clazz.getDeclaredMethods())
		{
			if (!method.getReturnType().equals(Void.TYPE))
				methods.add(method);
		}
		String[] result = new String[methods.size()];
		int i = 0;
		for (Method method : methods)
		{
			result[i] = method.getName();
			i++;
		}
		return result;
	}

	private String[] determineInterfaces(Class clazz)
	{
		List<Class> clazzes = new ArrayList();
		while (clazz != null && (!clazz.equals(Object.class)))
		{
			clazzes.addAll(Arrays.asList(clazz.getInterfaces()));
			clazz = clazz.getSuperclass();
		}
		String[] result = new String[clazzes.size()];
		int i = 0;
		for (Class claz : clazzes)
		{
			result[i] = claz.getName();
			i++;
		}
		return result;
	}

	public Long getId()
	{
		return _id;
	}

	public void invoke(CallbackReplyMessage message, ChannelHandlerContext ctx)
	{
		String methodName = message.getMethod();
		Object result = null;
		Exception fault = null;
		try
		{
			Object[] args = message.getArgs();
			if (args == null)
				args = new Object[0];
			Method[] methods = _callback.getClass().getMethods();
			for (Method method : methods)
			{
				if (methodName.equals(method.getName())
						&& method.getParameterTypes().length == args.length)
				{
					result = method.invoke(_callback, args);
					break;
				}
			}
		}
		catch (Exception ex)
		{
			Constants.ahessianLogger.warn("error invoking " + methodName, ex);
			fault = ex;
		}
		handleCallbackResult(fault, result, message, ctx);
	}

	private void handleCallbackResult(Object fault, Object result,
			CallbackReplyMessage message, ChannelHandlerContext ctx)
	{
		Map<Object, Object> headers = new HashMap();
		headers.put(Constants.ICALLBACK_CALL_ID_HEADER_KEY,
				message.getCallbackCallId());
		HessianRPCCallMessage call = new HessianRPCCallMessage(
				message.getCallbackMethod(), new Object[] { fault, result },
				headers, null);
		writeResult(call, ctx.channel());
	}

	private void writeResult(HessianRPCCallMessage reply, Channel channel)
	{
		channel.write(reply);
	}

	public String getCallbackClass()
	{
		return _callbackClass;
	}

	public String[] getInterfaces()
	{
		return _interfaces;
	}

	public static Object clientCallbackArgProxy(ClientCallback cc,
			ServerCallbackProxy serverCallbackProxy) throws Exception
	{
		ClassLoader cl = cc.getClass().getClassLoader();
		List<Class> clazzes = new ArrayList();
		for (String clz : cc.getInterfaces())
			try
			{
				clazzes.add(cl.loadClass(clz));
			}
			catch (Exception ex)
			{
				System.out.println("error loading: " + clz);
			}
		if (!clazzes.contains(Callback.class))
			clazzes.add(Callback.class);
		return Proxy.newProxyInstance(cl,
				(Class[]) clazzes.toArray(new Class[clazzes.size()]),
				serverCallbackProxy);

	}

	public String[] getReturnMethods()
	{
		return _returnMethods;
	}

}
