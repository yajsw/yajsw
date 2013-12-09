package org.rzo.netty.ahessian.rpc.server;

import org.rzo.netty.ahessian.io.InputStreamDecoder;
import org.rzo.netty.ahessian.session.ServiceSession;
import org.rzo.netty.ahessian.session.Session;

public class ServiceSessionProvider
{
	private static ThreadLocal<ServiceSession> threadLocalSession = new ThreadLocal<ServiceSession>();

	private static ThreadLocal<InputStreamDecoder> hessianRPCServiceHandler = new ThreadLocal<InputStreamDecoder>();

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
			((Session)threadLocalSession.get()).setNew(false);
		threadLocalSession.remove();
	}

	public static InputStreamDecoder getHandler()
	{
		//System.out.println("hessianRPCServiceHandler get ");
		return hessianRPCServiceHandler.get();
	}
	
	protected static void setHandler(InputStreamDecoder handler)
	{
		//System.out.println("hessianRPCServiceHandler set "+handler);
		hessianRPCServiceHandler.set(handler);
	}
	
	protected static void removeHandler()
	{
		//System.out.println("hessianRPCServiceHandler remove ");
		if (hessianRPCServiceHandler.get() == null)
			return;
		hessianRPCServiceHandler.remove();
	}

}
