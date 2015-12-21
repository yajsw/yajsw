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

import java.util.logging.Logger;

import org.rzo.yajsw.os.SystemInformation;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.DWORD;

public class WindowsXPSystemInformation implements SystemInformation
{

	public Logger _logger;
	private long _freeRAM = 0;
	private long _totalRAM = 0;
	private long _lastCall = 0;

	private void calc()
	{
		if (System.currentTimeMillis() - _lastCall < 500)
			return;
		WinBase.MEMORYSTATUSEX lpBuffer = new WinBase.MEMORYSTATUSEX();
		lpBuffer.dwLength = new DWORD(lpBuffer.size());
		if (Kernel32.INSTANCE.GlobalMemoryStatusEx(lpBuffer))
		{
			lpBuffer.read();
			_freeRAM = lpBuffer.ullAvailPhys.longValue();
			_totalRAM = lpBuffer.ullTotalPhys.longValue();
			_lastCall = System.currentTimeMillis();
		}
		else
		{
			if (_logger != null)
				_logger.severe("ERROR: could not read free/total RAM");
			else
				System.out.println("ERROR: could not read free/total RAM");
		}

	}

	public long freeRAM()
	{
		calc();
		return _freeRAM;
	}

	public long totalRAM()
	{
		calc();
		return _totalRAM;
	}

	public void setLogger(Logger logger)
	{
		_logger = logger;
	}

	public static void main(String[] args)
	{
		while (true)
		{
			System.out.println(new WindowsXPSystemInformation().totalRAM());
			System.out.println(new WindowsXPSystemInformation().freeRAM());
		}

	}

}
