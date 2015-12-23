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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;

public class WrapperGroovyMain extends AbstractWrapperJVMMain
{
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the args
	 * @throws IOException
	 * 
	 * @throws IllegalAccessException
	 *             *
	 * @throws InstantiationException
	 */
	public static void main(String[] args) throws IOException
	{
		preExecute(args);

		executeMain();

		postExecute();

	}

	protected static void executeMain()
	{
		String scriptName = WRAPPER_MANAGER.getGroovyScript();
		if (scriptName == null)
		{
			System.out.println("script not found in configuration -> aborting");
			System.exit(999);
		}
		File scriptFile = new File(scriptName);
		if (!scriptFile.exists())
		{
			System.out.println("script not found -> aborting: "
					+ scriptFile.getAbsolutePath());
			System.exit(999);
		}
		Object[] mainMethodArgs = WRAPPER_MANAGER.getMainMethodArgs();
		try
		{
			ClassLoader parent = WrapperGroovyMain.class.getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Class groovyClass = loader.parseClass(scriptFile);
			GroovyObject script = (GroovyObject) groovyClass.newInstance();
			script.invokeMethod("main", mainMethodArgs);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			exception = e;
		}
	}

}
