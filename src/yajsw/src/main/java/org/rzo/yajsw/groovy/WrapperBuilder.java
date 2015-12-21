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
package org.rzo.yajsw.groovy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.rzo.yajsw.boot.WrapperLoader;

public class WrapperBuilder extends HashMap // extends XHashMap
{
	static ClassLoader _wrapperClassLoader = WrapperLoader
			.getWrapperClassLoader();

	public Object process() throws ClassNotFoundException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		Thread.currentThread().setContextClassLoader(_wrapperClassLoader);
		Class cls = _wrapperClassLoader
				.loadClass("org.rzo.yajsw.wrapper.WrappedProcessFactory");

		Method create = cls.getDeclaredMethod("createProcess", Map.class);
		return create.invoke(null, this);
	}

	public Object service() throws ClassNotFoundException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		Thread.currentThread().setContextClassLoader(_wrapperClassLoader);
		Class cls = _wrapperClassLoader
				.loadClass("org.rzo.yajsw.wrapper.WrappedServiceFactory");
		Method create = cls.getDeclaredMethod("createService", Map.class);
		return create.invoke(null, this);
	}

}
