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

package org.rzo.yajsw.log;

import java.util.Arrays;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class MyLogger.
 */
public class MyLogger extends Logger
{

	/** The _name. */
	String _pid;
	String _name;

	/**
	 * Instantiates a new my logger.
	 */
	public MyLogger()
	{
		super(null, null);
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setPID(String pid)
	{
		_pid = pid;
	}

	public void setName(String name)
	{
		_name = name;
	}

	@Override
	public void log(LogRecord record)
	{
		Object[] newParams = null;
		Object[] params = record.getParameters();
		if (params == null || params.length == 0)
			newParams = new String[] { _pid, _name };
		else
		{
			int newSize = params.length + 2;
			newParams = Arrays.copyOf(params, newSize);
			newParams[newSize - 2] = _pid;
			newParams[newSize - 1] = _name;
		}

		record.setParameters(newParams);
		super.log(record);
	}
}
