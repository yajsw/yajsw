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

import java.lang.reflect.Method;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Interface WrapperManager.
 */
public interface WrapperManager
{

	/**
	 * Gets the main method.
	 * 
	 * @return the main method
	 */
	Method getMainMethod();

	/**
	 * Gets the main method args.
	 * 
	 * @return the main method args
	 */
	Object[] getMainMethodArgs();

	/**
	 * Checks if is exit on main terminate.
	 * 
	 * @return true, if is exit on main terminate
	 */
	int getExitOnMainTerminate();

	/**
	 * Inits the.
	 * 
	 * @param args
	 *            the args
	 * @param wrapperClassLoader
	 *            the wrapper class loader
	 */
	void init(String[] args, ClassLoader wrapperClassLoader);

	/**
	 * Start.
	 */
	void start();

	/**
	 * Thread dump.
	 */
	public void threadDump();

	/**
	 * Gets the pid.
	 * 
	 * @return the pid
	 */
	public int getPid();

	/**
	 * Stop.
	 */
	public void stop();

	public void restart();

	String getGroovyScript();

	int getExitOnException();

	public void reportServiceStartup();

	void executeScript(String scriptFileName, ClassLoader wrapperClassLoader);

	public void signalStopping(int timeoutHint);

	public Properties getProperties();

	public String getStopReason();

	public void setShutdownListener(Runnable listener);
}
