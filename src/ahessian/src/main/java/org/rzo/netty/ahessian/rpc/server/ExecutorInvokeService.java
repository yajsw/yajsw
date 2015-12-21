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
import java.util.concurrent.Executor;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.callback.ClientCallback;
import org.rzo.netty.ahessian.rpc.callback.ServerCallbackProxy;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;

/**
 * Wraps an object as a {@link Service}. Methods are invoked as soon as they are
 * received. Invocation and return of result are executed within a thread of the
 * given Executor. <br>
 * This type of service is used for invocations with different execution
 * durations. Invokation hangs if all threads in the pool are exhausted <br>
 * Typical usage:
 * 
 * <pre>
 * 
 * // the object to be wrapped, implements MyServiceInterface
 * Object myServiceObject;
 * 
 * // the netty rpc service handler
 * HessianRPCServiceHandler handler;
 * Executor executor = Executors.newFixedThreadPool(200);
 * Service myService = new ExecutorInvokeService(myServiceObject,
 * 		MyServiceInterface.class, executor);
 * 
 * // Clients will access the service through the given name
 * handler.addService(&quot;myServiceName&quot;, myService);
 * 
 * </pre>
 */

public class ExecutorInvokeService extends HessianSkeleton implements Constants
{
	// public static ThreadLocal threadLocalSession = new ThreadLocal();
	Executor _executor;

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
	public ExecutorInvokeService(Object service, Class apiClass,
			HessianRPCServiceHandler factory, Executor executor)
	{
		super(service, apiClass, factory);
		_executor = executor;
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
		// threadLocalSession.set(ServerSessionFilter.getSession(ctx));
		invoke(message);
	}

	/**
	 * Invokes the RPC call and sends back the result
	 * 
	 * @param message
	 *            the message
	 */
	void invoke(final HessianRPCCallMessage message)
	{
		_executor.execute(new Runnable()
		{

			public void run()
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
								args[i] = ClientCallback
										.clientCallbackArgProxy(cc,
												new ServerCallbackProxy(
														_factory, message, cc));
							}
						}
					}
					ServiceSessionProvider.set(message.getSession());
					ServiceSessionProvider.setHandler(message.getHandler());
					threadLocalChannel.set(message.getChannel());

					result = method.invoke(_service, args);

					threadLocalChannel.remove();
					ServiceSessionProvider.remove();
					ServiceSessionProvider.removeHandler();
				}
				catch (Throwable ex)
				{
					ServiceSessionProvider.remove();
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

		});
	}

}
