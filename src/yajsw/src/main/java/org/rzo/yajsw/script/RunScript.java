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
package org.rzo.yajsw.script;

import java.io.File;
import java.util.Iterator;

import org.rzo.yajsw.boot.WrapperLoader;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedProcessFactory;

public class RunScript
{

	public static void main(String[] args)
	{
		String wrapperJar = WrapperLoader.getWrapperJar();
		String homeDir = new File(wrapperJar).getParent();
		if (!OperatingSystem.instance().setWorkingDir(homeDir))
			System.out
					.println("could not set working dir. pls check configuration or user rights :"
							+ homeDir);

		String configFile = args[0];
		String script = args[1];
		int count = args.length == 3 ? Integer.parseInt(args[3]) : 1;

		// get the script arguments from the configuration
		String scriptKey = null;
		System.setProperty("wrapper.config", configFile);
		YajswConfigurationImpl config = new YajswConfigurationImpl(true);
		for (Iterator it = config.getKeys(); it.hasNext();)
		{
			String key = (String) it.next();
			if (key.contains(".script.") && !key.endsWith(".args")
					&& config.getString(key).equals(script))
			{
				scriptKey = key;
				break;
			}
		}
		if (scriptKey == null)
		{
			System.out.println("script not found in configuration -> abort");
			return;
		}
		String[] scriptArgs = config.getStringArray(scriptKey + ".args");

		// get a dummy process but do not start it
		WrappedProcess process = WrappedProcessFactory.createProcess(config);
		process.init();

		Script s = ScriptFactory.createScript(script, "test", process,
				scriptArgs, null, 0,
				config.getString("wrapper.script.encoding"),
				config.getBoolean("wrapper.script.reload", false), 3, 1);
		if (s == null)
		{
			System.out.println("error initializing script " + script);
			return;
		}
		for (int i = 0; i < count; i++)
			s.execute();
		Runtime.getRuntime().halt(0);
	}

}
