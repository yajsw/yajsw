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

import java.io.IOException;
import java.lang.reflect.Method;

// TODO: Auto-generated Javadoc
/**
 * The Class WrapperMain.
 */
public class WrapperJVMMain extends AbstractWrapperJVMMain
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
		final Method mainMethod = WRAPPER_MANAGER.getMainMethod();
		if (mainMethod == null)
		{
			System.out.println("no java main method found -> aborting");
			System.exit(999);
		}
		Object[] mainMethodArgs = WRAPPER_MANAGER.getMainMethodArgs();
		try
		{
			mainMethod.invoke(null, new Object[] { mainMethodArgs });
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			exception = e;
		}
	}

}
