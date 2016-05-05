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
package org.rzo.yajsw.os.ms.win.w32;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.rzo.yajsw.script.Script;
import org.rzo.yajsw.script.ScriptFactory;
import org.rzo.yajsw.util.VFSUtils;

public class ScriptCounter implements PdhCounter
{
	InternalLogger _log = InternalLoggerFactory.getInstance(getClass()
			.getName());
	Script _script;
	private String _scriptFile;
	String _counterString;
	private String[] _args;
	private long _lastModified;

	/**
	 * Format "\ScriptCounter(abc.groovy)\arg1,arg2,..."
	 */
	public ScriptCounter(String counterString)
	{
		init(counterString);
		checkScript();

	}

	private void init(String counterString)
	{
		_counterString = counterString;
		_scriptFile = StringUtils.substringBetween(counterString, "(", ")");
		String argsString = StringUtils.substringAfter(counterString, ")\\");
		if (argsString != null)
			_args = StringUtils.split(argsString, ",");
	}

	private void checkScript()
	{
		long lastModified;
		try
		{
			lastModified = VFSUtils.resolveFile(".", _scriptFile).getContent()
					.getLastModifiedTime();
		}
		catch (FileSystemException e)
		{
			throw new IllegalArgumentException("Cannot find script "
					+ _scriptFile + " ex=" + e.getMessage());
		}
		if (_lastModified == lastModified)
			return;
		else
		{
			_lastModified = lastModified;
			_script = ScriptFactory.createScript(_scriptFile, _counterString,
					null, _args, _log, 0, null, false, 0, 1);
			if (_script == null)
				throw new IllegalArgumentException("Cannot find script "
						+ _scriptFile);
		}
	}

	public void close()
	{
		// nothing
	}

	public double getDoubleValue()
	{
		checkScript();
		Object result = _script.execute();

		if (result instanceof Number)
			return ((Number) result).doubleValue();
		else
			return Double.parseDouble((String) result);
	}

	public int getIntValue()
	{
		checkScript();
		Object result = _script.execute();
		if (result instanceof Number)
			return ((Number) result).intValue();
		else
			return Integer.parseInt((String) result);
	}

	public long getLongValue()
	{
		checkScript();
		Object result = _script.execute();
		if (result instanceof Number)
			return ((Number) result).intValue();
		else
			return Long.parseLong((String) result);
	}

	public boolean isValid()
	{
		try
		{
			getIntValue();
			return true;
		}
		catch (Exception e)
		{
			try
			{
				getDoubleValue();
				return true;
			}
			catch (Exception e2)
			{
				return false;
			}
		}
	}
}
