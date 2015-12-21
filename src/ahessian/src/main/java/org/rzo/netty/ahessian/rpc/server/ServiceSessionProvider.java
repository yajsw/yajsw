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

import org.rzo.netty.ahessian.io.InputStreamHandler;
import org.rzo.netty.ahessian.session.ServiceSession;
import org.rzo.netty.ahessian.session.Session;

public class ServiceSessionProvider
{
	private static ThreadLocal<ServiceSession> threadLocalSession = new ThreadLocal<ServiceSession>();

	private static ThreadLocal<InputStreamHandler> hessianRPCServiceHandler = new ThreadLocal<InputStreamHandler>();

	public static ServiceSession getSession()
	{
		return threadLocalSession.get();
	}

	protected static void set(ServiceSession session)
	{
		threadLocalSession.set(session);
	}

	protected static void remove()
	{
		if (threadLocalSession.get() == null)
			return;
		if (threadLocalSession.get().isNew())
			((Session) threadLocalSession.get()).setNew(false);
		threadLocalSession.remove();
	}

	public static InputStreamHandler getHandler()
	{
		// System.out.println("hessianRPCServiceHandler get ");
		return hessianRPCServiceHandler.get();
	}

	protected static void setHandler(InputStreamHandler handler)
	{
		// System.out.println("hessianRPCServiceHandler set "+handler);
		hessianRPCServiceHandler.set(handler);
	}

	protected static void removeHandler()
	{
		// System.out.println("hessianRPCServiceHandler remove ");
		if (hessianRPCServiceHandler.get() == null)
			return;
		hessianRPCServiceHandler.remove();
	}

}
