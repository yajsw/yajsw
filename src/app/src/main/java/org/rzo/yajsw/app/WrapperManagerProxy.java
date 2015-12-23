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

import java.net.URL;

import org.rzo.yajsw.boot.WrapperClassLoader;
import org.rzo.yajsw.boot.WrapperLoader;

// TODO: Auto-generated Javadoc
/**
 * The Class WrapperMangerProxy.
 */
public class WrapperManagerProxy
{

	/** The wrapper class loader. */
	static ClassLoader wrapperClassLoader;

	/**
	 * Gets the wrapper manager.
	 * 
	 * @param args
	 *            the args
	 * 
	 * @return the wrapper manager
	 */
	public static WrapperManager getWrapperManager(final String[] args)
	{
		wrapperClassLoader = getWrapperClassLoader();
		Class wrapperManagerClass;
		final WrapperManager wm;
		WrapperManager result = null;
		try
		{
			wrapperManagerClass = wrapperClassLoader
					.loadClass("org.rzo.yajsw.app.WrapperManagerImpl");
			wm = (WrapperManager) wrapperManagerClass.newInstance();
			wm.init(args, wrapperClassLoader);
			// start the wrapper manager in a separate thread
			// so application may start even if communication to controller
			// takes time

			new Thread(new Runnable()
			{

				public void run()
				{
					try
					{
						wm.start();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}, "yajsw.app.manager.start").start();
			// give the manager.start a chance.
			Thread.yield();
			result = wm;
			String preScript = System
					.getProperty("wrapper.app.pre_main.script");
			if (preScript != null & !"".equals(preScript))
			{
				// Logger logger = new MyLogger();
				// logger.addHandler(new ConsoleHandler());
				wm.executeScript(preScript, wrapperClassLoader);
			}

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		return result;
	}

	/**
	 * Gets the wrapper class loader.
	 * 
	 * @return the wrapper class loader
	 */
	private static ClassLoader getWrapperClassLoader()
	{
		URL[] urlsArr = WrapperLoader.getWrapperClasspath("App", true);
		if (urlsArr == null)
			return Thread.currentThread().getContextClassLoader();
		URL[] extended = WrapperLoader.getWrapperClasspath("App-Extended",
				false);
		URL[] urls = new URL[urlsArr.length + extended.length];
		System.arraycopy(urlsArr, 0, urls, 0, urlsArr.length);
		System.arraycopy(extended, 0, urls, urlsArr.length, extended.length);

		return new WrapperClassLoader(urls, ClassLoader.getSystemClassLoader());
	}

}
