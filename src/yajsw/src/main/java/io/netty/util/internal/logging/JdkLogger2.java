/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <a href=
 * "http://java.sun.com/javase/6/docs/technotes/guides/logging/index.html"
 * >java.util.logging</a> logger.
 * 
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * 
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 * 
 */
class JdkLogger2 implements InternalLogger
{

	private final Logger logger;

	JdkLogger2(Logger logger)
	{

		this.logger = logger;
	}

	public void debug(String msg)
	{
		logger.log(Level.FINE, msg);
	}

	public void debug(String msg, Throwable cause)
	{
		logger.log(Level.FINE, msg, cause);
	}

	public void error(String msg)
	{
		logger.log(Level.SEVERE, msg);
	}

	public void error(String msg, Throwable cause)
	{
		logger.log(Level.SEVERE, msg, cause);
	}

	public void info(String msg)
	{
		logger.log(Level.INFO, msg);
	}

	public void info(String msg, Throwable cause)
	{
		logger.log(Level.INFO, msg, cause);
	}

	public boolean isDebugEnabled()
	{
		return logger.isLoggable(Level.FINE);
	}

	public boolean isErrorEnabled()
	{
		return logger.isLoggable(Level.SEVERE);
	}

	public boolean isInfoEnabled()
	{
		return logger.isLoggable(Level.INFO);
	}

	public boolean isWarnEnabled()
	{
		return logger.isLoggable(Level.WARNING);
	}

	public void warn(String msg)
	{
		logger.log(Level.WARNING, msg);
	}

	public void warn(String msg, Throwable cause)
	{
		logger.log(Level.WARNING, msg, cause);
	}

	@Override
	public String toString()
	{
		return logger.toString();
	}

	@Override
	public void debug(String msg, Object paramObject)
	{
		logger.log(Level.FINE, msg, paramObject);
	}

	@Override
	public void debug(String msg, Object... paramArrayOfObject)
	{
		logger.log(Level.FINE, msg, paramArrayOfObject);
	}

	@Override
	public void debug(String msg, Object paramObject1, Object paramObject2)
	{
		logger.log(Level.FINE, msg, new Object[] { paramObject1, paramObject2 });
	}

	@Override
	public void error(String msg, Object paramObject)
	{
		logger.log(Level.SEVERE, msg, paramObject);
	}

	@Override
	public void error(String msg, Object... paramArrayOfObject)
	{
		logger.log(Level.SEVERE, msg, paramArrayOfObject);
	}

	@Override
	public void error(String msg, Object paramObject1, Object paramObject2)
	{
		logger.log(Level.SEVERE, msg,
				new Object[] { paramObject1, paramObject2 });
	}

	@Override
	public void info(String msg, Object paramObject)
	{
		logger.log(Level.INFO, msg, paramObject);
	}

	@Override
	public void info(String msg, Object... paramArrayOfObject)
	{
		logger.log(Level.INFO, msg, paramArrayOfObject);
	}

	@Override
	public void info(String msg, Object paramObject1, Object paramObject2)
	{
		logger.log(Level.INFO, msg, new Object[] { paramObject1, paramObject2 });
	}

	@Override
	public boolean isTraceEnabled()
	{
		return logger.isLoggable(Level.INFO);
	}

	@Override
	public void trace(String msg)
	{
		info(msg);
	}

	@Override
	public void trace(String msg, Object paramObject)
	{
		info(msg, paramObject);
	}

	@Override
	public void trace(String msg, Object... paramArrayOfObject)
	{
		info(msg, paramArrayOfObject);
	}

	@Override
	public void trace(String msg, Throwable paramThrowable)
	{
		info(msg, paramThrowable);
	}

	@Override
	public void trace(String msg, Object paramObject1, Object paramObject2)
	{
		info(msg, paramObject1, paramObject2);
	}

	@Override
	public void warn(String msg, Object paramObject)
	{
		error(msg, paramObject);
	}

	@Override
	public void warn(String msg, Object... paramArrayOfObject)
	{
		error(msg, paramArrayOfObject);
	}

	@Override
	public void warn(String msg, Object paramObject1, Object paramObject2)
	{
		error(msg, paramObject1, paramObject2);
	}

	@Override
	public String name()
	{
		return logger.getName();
	}

	@Override
	public boolean isEnabled(InternalLogLevel paramInternalLogLevel)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void log(InternalLogLevel paramInternalLogLevel, String paramString)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void log(InternalLogLevel paramInternalLogLevel, String paramString,
			Object paramObject)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void log(InternalLogLevel paramInternalLogLevel, String paramString,
			Object paramObject1, Object paramObject2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void log(InternalLogLevel paramInternalLogLevel, String paramString,
			Object... paramArrayOfObject)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void log(InternalLogLevel paramInternalLogLevel, String paramString,
			Throwable paramThrowable)
	{
		// TODO Auto-generated method stub

	}

}
