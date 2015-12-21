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

import io.netty.util.internal.logging.InternalLogger;

import java.util.List;

import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Script objects.
 */
public class ScriptFactory
{

	/**
	 * Creates a new Script object.
	 * 
	 * @param script
	 *            the script
	 * @param timeout
	 * 
	 * @return the script
	 */
	public static Script createScript(String script, String id,
			WrappedProcess process, String[] args, InternalLogger log,
			int timeout, String encoding, boolean reload, int debug,
			int maxConcInvocations)
	{
		if (script == null || "".equals(script))
			return null;
		if (log != null && debug > 1)
			log.info("create script: " + script);
		if (script.endsWith(".bat") || script.endsWith(".sh"))
			return new ShellScript(script, id, process, args, timeout,
					maxConcInvocations);
		if (script.endsWith(".gv") || script.endsWith(".groovy"))
			try
			{
				return new GroovyScript(script, id, process, args, timeout,
						log, encoding, reload, maxConcInvocations);
			}
			catch (Throwable e)
			{
				if (log != null)
					log.info("Error in createScript " + script, e);
			}
		return null;
	}

	public static Script createScript(String script, String id,
			WrappedProcess process, List args, InternalLogger log, int timeout,
			String encoding, boolean reload, int debug, int maxConcInvocations)
	{
		String[] argsArr = new String[0];
		if (args != null && args.size() > 0)
		{
			argsArr = new String[args.size()];
			for (int i = 0; i < argsArr.length; i++)
				argsArr[i] = args.get(i).toString();
		}
		return createScript(script, id, process, argsArr, log, timeout,
				encoding, reload, debug, maxConcInvocations);
	}

}
