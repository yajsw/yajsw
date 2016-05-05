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
package org.rzo.yajsw.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.rzo.yajsw.config.YajswConfiguration;
import org.rzo.yajsw.config.YajswConfigurationImpl;

public class WrappedProcessFactory
{
	public static WrappedProcess createProcess(YajswConfiguration config)
	{
		if (config.getString("wrapper.image") != null)
			if ("true".equals(config.getString("wrapper.image.javawrapper")))
				return new WrappedRuntimeJavaProcess();
			else
				return new WrappedRuntimeProcess();
		else if (config.getString("wrapper.groovy") != null)
			return new WrappedGroovyProcess();
		return new WrappedJavaProcess();
	}

	public static WrappedProcess createProcess(Map map,
			boolean useSystemProperties)
	{
		Configuration localConf = new MapConfiguration(map);
		YajswConfiguration conf = new YajswConfigurationImpl(localConf, true);
		WrappedProcess process = createProcess(conf);
		process.setLocalConfiguration(localConf);
		process.setUseSystemProperties(useSystemProperties);
		process.init();
		return process;
	}

	public static WrappedProcessList createProcessList(Map map,
			List<Object> confFiles, boolean useSystemProperties)
	{
		WrappedProcessList list = new WrappedProcessList();
		for (Object conf : confFiles)
		{
			Map sConf = new HashMap(map);
			sConf.put("wrapper.config", conf);
			list.add(createProcess(sConf, useSystemProperties));
		}
		return list;
	}

}
