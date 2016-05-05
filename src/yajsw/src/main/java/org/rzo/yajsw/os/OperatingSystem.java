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

package org.rzo.yajsw.os;

import org.apache.commons.configuration2.Configuration;
import org.rzo.yajsw.os.ms.win.w32.OperatingSystemWindowsXP;
import org.rzo.yajsw.os.posix.bsd.OperatingSystemBSD;
import org.rzo.yajsw.os.posix.bsd.macosx.OperatingSystemMacOsX;
import org.rzo.yajsw.os.posix.linux.OperatingSystemLinux;
import org.rzo.yajsw.os.posix.solaris.OperatingSystemSolaris;

// TODO: Auto-generated Javadoc
/**
 * The Class OperatingSystem.
 */
public abstract class OperatingSystem
{

	/** The _instance. */
	static OperatingSystem _instance;

	/** The _os name. */
	static String _osName;

	static boolean _isPosix = true;

	/**
	 * Instance.
	 * 
	 * @return the operating system
	 */
	public static OperatingSystem instance()
	{
		if (_instance != null)
			return _instance;
		_osName = System.getProperty("os.name");
		if (_osName.toLowerCase().startsWith("windows"))
		{
			_instance = new OperatingSystemWindowsXP();
			_isPosix = false;
		}
		else if (_osName.toLowerCase().startsWith("mac os x"))
			_instance = new OperatingSystemMacOsX();
		else if (_osName.contains("BSD"))
			_instance = new OperatingSystemBSD();
		else if (_osName.contains("AIX"))
			_instance = new OperatingSystemBSD();
		else if (_osName.toLowerCase().startsWith("linux"))
			_instance = new OperatingSystemLinux();
		else if (_osName.toLowerCase().contains("sunos"))
			_instance = new OperatingSystemSolaris();
		if (_instance == null)
			System.out.println("OS not supported " + _osName);
		return _instance;

	}

	/**
	 * Gets the operating system name.
	 * 
	 * @return the operating system name
	 */
	public String getOperatingSystemName()
	{
		return _osName;
	}

	public boolean isPosix()
	{
		return _isPosix;
	}

	/**
	 * Keyboard instance.
	 * 
	 * @return the keyboard
	 */
	public abstract Keyboard keyboardInstance();

	public abstract Mouse mouseInstance();

	/**
	 * Process manager instance.
	 * 
	 * @return the process manager
	 */
	public abstract ProcessManager processManagerInstance();

	public abstract FileManager fileManagerInstance();

	/**
	 * Service manager instance.
	 * 
	 * @return the process manager
	 */
	public abstract ServiceManager serviceManagerInstance();

	/**
	 * Error handler instance.
	 * 
	 * @return the error handler
	 */
	public abstract ErrorHandler errorHandlerInstance();

	public abstract JavaHome getJavaHome(Configuration config);

	public abstract Object getServiceFailureActions(Configuration config);

	public abstract SystemInformation systemInformation();

	public abstract boolean setWorkingDir(String name);
	
	// returns seconds since boot, on windows max value is apprx 49 days
	public abstract long getUptime();
	
	public abstract void reboot();

}
