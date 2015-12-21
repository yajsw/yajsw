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
package org.rzo.yajsw.os.posix;

import java.util.logging.Logger;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class VelocityLog implements LogChute
{
	static Logger _logger = null;

	static public void setLogger(Logger logger)
	{
		_logger = logger;
	}

	public void init(RuntimeServices arg0) throws Exception
	{
		// TODO Auto-generated method stub

	}

	public boolean isLevelEnabled(int arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void log(int arg0, String arg1)
	{
		// TODO Auto-generated method stub

	}

	public void log(int arg0, String arg1, Throwable arg2)
	{
		if (_logger != null)
			_logger.throwing(VelocityLog.class.getName(), arg1, arg2);
	}

}
