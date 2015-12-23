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
import java.net.URLClassLoader;

// TODO: Auto-generated Javadoc
/**
 * The Class WrapperManagerClassLoader.
 */
public class WrapperManagerClassLoader extends URLClassLoader
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
	public WrapperManagerClassLoader(URL[] urls, ClassLoader parent)
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

				}
				catch (ClassNotFoundException e)
				{
					// If still not found, then invoke findClass in order
					// to find the class.
				}
		}
		if (c == null)
			if (_parent != null)
				c = _parent.loadClass(name);

		return c;

	}

}
