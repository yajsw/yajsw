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

package org.rzo.yajsw.boot;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class loader for handling YAJSW related libraries. The classloader will first
 * search the YAJSW related libraries before searching the parent (system
 * classloader). The libraries required by YAJSW will thus not be visible to the
 * application which is started within the system classloader. Exception is for
 * the interface WrapperManager and for the class WrapperManagerProxy. An
 * singleton instance of WrapperManager is visible to the application for
 * shutdown or for restart. WrapperManagerProxy is required for on application
 * startup.
 */
public class WrapperClassLoader extends URLClassLoader
{

	/** The _parent. */
	ClassLoader _parent;

	/**
	 * Instantiates a new wrapper manager class loader.
	 * 
	 * @param urls
	 *            the urls
	 * @param parent
	 *            the parent
	 */
	public WrapperClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, null);
		_parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
	public synchronized Class<?> findClass(String name)
			throws ClassNotFoundException
	{
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if (c == null)
		{
			if (!"org.rzo.yajsw.app.WrapperManager".equals(name)
					&& !"org.rzo.yajsw.app.WrapperManagerProxy".equals(name))
				try
				{
					c = super.findClass(name);
					// System.out.println("got wrapper class "+name);

				}
				catch (ClassNotFoundException e)
				{
					// If still not found, then invoke findClass in order
					// to find the class.
				}
		}
		if (c == null)
			if (_parent != null)
			{
				c = _parent.loadClass(name);
				// System.out.println("got main class "+name);

			}

		return c;

	}

}
