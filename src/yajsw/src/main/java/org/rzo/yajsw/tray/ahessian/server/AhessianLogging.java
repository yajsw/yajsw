package org.rzo.yajsw.tray.ahessian.server;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.logging.Logger;

public class AhessianLogging
{
	public static void setAhessianLogger(final Logger log)
	{
		InternalLoggerFactory.setDefaultFactory(new InternalLoggerFactory()
		{

			@Override
			public InternalLogger newInstance(String name)
			{
				return (InternalLogger) new JdkLogger(log, "ahessian-jmx" );
			}			
		});
	}

}
