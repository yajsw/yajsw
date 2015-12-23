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
package org.rzo.yajsw.app;

import java.security.AccessController;
import java.security.PrivilegedAction;

public abstract class AbstractWrapperJVMMain
{

	/** The WRAPPE r_ manager. */
	public static WrapperManager WRAPPER_MANAGER;
	public static Throwable exception = null;

	// call java logger, so that it inits before groovy & co
	// private static Logger dummy = Logger.getAnonymousLogger();

	static class YajswUncaughtExceptionHandler implements
			Thread.UncaughtExceptionHandler
	{
		public void uncaughtException(Thread t, Throwable e)
		{
			System.err.println("Uncaught exception by " + t + ":");
			System.err.println(e.getClass().getName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}

	protected static void postExecute()
	{
		int exitCode;
		if (exception == null)
			exitCode = WRAPPER_MANAGER.getExitOnMainTerminate();
		else
			exitCode = WRAPPER_MANAGER.getExitOnException();
		if (exitCode >= 0)
			System.exit(exitCode);
	}

	protected static void preExecute(String[] args)
	{
		final String[] finalArgs = args;
		WRAPPER_MANAGER = (WrapperManager) AccessController
				.doPrivileged(new PrivilegedAction<Object>()
				{
					public Object run()
					{
						// set our own handler so that we may log out of memory
						// errors
						Thread.setDefaultUncaughtExceptionHandler(new YajswUncaughtExceptionHandler());
						WrapperManager result = WrapperManagerProxy
								.getWrapperManager(finalArgs);
						return result;
					}
				});
	}

}
