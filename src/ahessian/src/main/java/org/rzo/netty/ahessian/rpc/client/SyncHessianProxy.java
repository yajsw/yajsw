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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A proxy object implementing synchronous invocations. <br>
 * Typical usage:
 * 
 * <pre>
 * HessianProxyFactory proxyFactory = ...
 * 
 * Map options = new HashMap();
 * options.put(&quot;id&quot;, &quot;myServiceName&quot;);
 * options.put(&quot;synch&quot;, Boolean.TRUE);
 * 
 * // AsynchMyServiceInterface is an interface including the same methods as MyServiceInterface 
 * //except that the return type is always of type HessianProxyFuture
 * AsynchMyServiceInterface service = (AsynchMyServiceInterface) factory.create(AsynchMyServiceInterface.class, getClassLoader(), options);
 * 
 * // invoke a service method and wait for the result
 * Object result = service.myMethod();
 * </pre>
 */
public class SyncHessianProxy implements InvocationHandler
{

	/** The _handler. */
	AsyncHessianProxy _handler;
	long _defaultTimeout = -1;

	/**
	 * Instantiates a new sync hessian proxy.
	 * 
	 * @param handler
	 *            the handler
	 */
	SyncHessianProxy(InvocationHandler handler)
	{
		_handler = (AsyncHessianProxy) handler;
		if (_handler._options != null
				&& _handler._options.get("timeout") != null)
			_defaultTimeout = ((Long) _handler._options.get("timeout"))
					.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{
		if ("equals".equals(method.getName())
				|| "hashCode".equals(method.getName())
				|| "getHessianType".equals(method.getName())
				|| "getHessianURL".equals(method.getName()))
			return _handler.invoke(proxy, method, args);
		if ("toString".equals(method.getName()))
			return "Sync:" + _handler.invoke(proxy, method, args);
		long timeout = getTimeout(method.getName());
		if (timeout > 0)
			return ((Future) _handler.invoke(proxy, method, args)).get(timeout,
					TimeUnit.MILLISECONDS);
		else
			return ((Future) _handler.invoke(proxy, method, args)).get();
	}

	private long getTimeout(String method)
	{
		if (_handler._options != null
				&& _handler._options.get("timeout." + method) != null)
			return ((Long) _handler._options.get("timeout." + method))
					.longValue();
		return _defaultTimeout;
	}

}
