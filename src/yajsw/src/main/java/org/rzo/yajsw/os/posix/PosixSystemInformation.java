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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rzo.yajsw.os.SystemInformation;

public class PosixSystemInformation implements SystemInformation
{
	PosixUtils _utils = new PosixUtils();
	Logger _logger;

	public void setLogger(Logger logger)
	{
		_logger = logger;
	}

	public long freeRAM()
	{
		String info = _utils.readFile("/proc/meminfo");
		if (info != null)
			try
			{
				String sp = ".*MemFree:\\s*(\\d+) kB.*";
				Pattern p = Pattern.compile(sp, Pattern.DOTALL);
				Matcher m = p.matcher(info);
				m.find();
				return Long.parseLong(m.group(1)) * 1024;
			}
			catch (Exception ex)
			{
				if (_logger != null)
					_logger.throwing(PosixSystemInformation.class.getName(),
							"freeRAM", ex);
			}
		return 0;
	}

	public long totalRAM()
	{
		String info = _utils.readFile("/proc/meminfo");
		if (info != null)
			try
			{
				String sp = ".*MemTotal:\\s*(\\d+) kB.*";
				Pattern p = Pattern.compile(sp, Pattern.DOTALL);
				Matcher m = p.matcher(info);
				m.find();
				return Long.parseLong(m.group(1)) * 1024;
			}
			catch (Exception ex)
			{
				if (_logger != null)
					_logger.throwing(PosixSystemInformation.class.getName(),
							"totalRAM", ex);
			}
		return 0;
	}

}
