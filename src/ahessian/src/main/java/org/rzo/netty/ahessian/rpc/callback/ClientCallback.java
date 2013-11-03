package org.rzo.netty.ahessian.rpc.callback;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.rzo.netty.ahessian.Constants;


public class ClientCallback implements Serializable
{

	private transient Callback _callback;
	private Long _id;
	private String _callbackClass;
	private String[] _interfaces;
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

	public void invoke(CallbackReplyMessage message)
	{
		try
		{
		String methodName = message.getMethod();
		Object[] args = message.getArgs();
		if (args == null)
			args = new Object[0];
		Method[] methods = _callback.getClass().getMethods();
		for (Method method : methods)
		{
			if (methodName.equals(method.getName()) && method.getParameterTypes().length == args.length)
			{
				method.invoke(_callback, args);
				break;
			}
		}
		}
		catch (Exception ex)
		{
			Constants.ahessianLogger.warn("", ex);
		}
	}

	public String getCallbackClass()
	{
		return _callbackClass;
	}


	public String[] getInterfaces()
	{
		return _interfaces;
	}
	
	public static Object clientCallbackArgProxy(ClientCallback cc, ServerCallbackProxy serverCallbackProxy) throws Exception
	{
		ClassLoader cl = cc.getClass().getClassLoader();
		List<Class> clazzes = new ArrayList();
		for (String clz : cc.getInterfaces())
			clazzes.add(cl.loadClass(clz));
		return Proxy.newProxyInstance(cl, (Class[])clazzes.toArray(new Class[clazzes.size()]), serverCallbackProxy);

	}
	
}
