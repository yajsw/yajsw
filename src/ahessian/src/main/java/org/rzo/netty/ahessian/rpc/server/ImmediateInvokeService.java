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
package org.rzo.netty.ahessian.rpc.server;

import java.io.InputStream;
import java.lang.reflect.Method;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.callback.ClientCallback;
import org.rzo.netty.ahessian.rpc.callback.ServerCallbackProxy;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;

/**
 * Wraps an object as a {@link Service}. Methods are invoked as soon as they are
 * received. Invocation and return of result are executed within the netty
 * worker thread. <br>
 * This type of service is used for short running invocations. <br>
 * Typical usage:
 * 
 * <pre>
 * 
 * // the object to be wrapped, implements MyServiceInterface
 * Object myServiceObject;
 * 
 * // the netty rpc service handler
 * HessianRPCServiceHandler handler;
 * 
 * Service myService = new ImmediateInvokeService(myServiceObject,
 * 		MyServiceInterface.class);
 * 
 * // Clients will access the service through the given name
 * handler.addService(&quot;myServiceName&quot;, myService);
 * 
 * </pre>
 */
public class ImmediateInvokeService extends HessianSkeleton implements
		Constants
{

	/**
	 * Instantiates a new immediate invoke service.
	 * 
	 * @param service
	 *            the service object implementing apiClass
	 * @param apiClass
	 *            the api of the service exposed to the client
	 * @param factory
	 *            the netty handler
	 */

	public ImmediateInvokeService(Object service, Class apiClass,
			HessianRPCServiceHandler factory)
	{
		super(service, apiClass, factory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rzo.netty.ahessian.rpc.server.HessianSkeleton#messageReceived(org
	 * .rzo.netty.ahessian.rpc.HessianRPCCallMessage)
	 */
	@Override
	public void messageReceived(HessianRPCCallMessage message)
	{
		ServiceSessionProvider.set(message.getSession());
		ServiceSessionProvider.setHandler(message.getHandler());
		threadLocalChannel.set(message.getChannel());
		invoke(message);
		threadLocalChannel.remove();
		ServiceSessionProvider.remove();
		ServiceSessionProvider.removeHandler();
	}

	/**
	 * Invokes the RPC call and sends back the result
	 * 
	 * @param message
	 *            the message
	 */
	void invoke(HessianRPCCallMessage message)
	{
		Object result = null;
		Object fault = null;
		try
		{
			Method method = getMethod(message);
			Object[] args = message.getArgs();
			if (args != null)
			{
				for (int i = 0; i < args.length; i++)
				{
					if (args[i] instanceof ClientCallback)
					{
						ClientCallback cc = (ClientCallback) args[i];
						args[i] = ClientCallback.clientCallbackArgProxy(cc,
								new ServerCallbackProxy(_factory, message, cc));
					}
				}
			}
			result = method.invoke(_service, args);
		}
		catch (Throwable ex)
		{
			Constants.ahessianLogger.warn("", ex);
			fault = ex;
		}
		if (fault == null && result instanceof InputStream)
		{
			handleInputStreamResult(fault, result, message);
		}
		else
		{
			handleDefaultResult(fault, result, message);
		}
	}

}
