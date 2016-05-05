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

import org.apache.commons.configuration2.Configuration;
import org.rzo.yajsw.os.ErrorHandler;
import org.rzo.yajsw.os.FileManager;
import org.rzo.yajsw.os.JavaHome;
import org.rzo.yajsw.os.Keyboard;
import org.rzo.yajsw.os.Mouse;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.ProcessManager;
import org.rzo.yajsw.os.ServiceManager;
import org.rzo.yajsw.os.SystemInformation;

// TODO: Auto-generated Javadoc
/**
 * The Class OperatingSystemWindowsXP.
 */
public class OperatingSystemWindowsXP extends OperatingSystem
{

	/** The _keyboard instance. */
	static Keyboard _keyboardInstance;
	static Mouse _mouseInstance;

	/** The _process manager. */
	static ProcessManager _processManager;
	static FileManager _fileManager;

	/** The _process manager. */
	static ServiceManager _serviceManager;

	/** The _error handler. */
	static ErrorHandler _errorHandler = new WindowsXPErrorHandler();

	static SystemInformation _systemInformation = new WindowsXPSystemInformation();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.OperatingSystem#keyboardInstance()
	 */
	@Override
	public Keyboard keyboardInstance()
	{
		if (_keyboardInstance == null)
			_keyboardInstance = WindowsXPKeyboard.instance();
		return _keyboardInstance;
	}

	public Mouse mouseInstance()
	{
		if (_mouseInstance == null)
			_mouseInstance = WindowsXPMouse.instance();
		return _mouseInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.OperatingSystem#processManagerInstance()
	 */
	@Override
	public ProcessManager processManagerInstance()
	{
		if (_processManager == null)
			_processManager = WindowsXPProcessManager.instance();
		return _processManager;
	}

	public FileManager fileManagerInstance()
	{
		if (_fileManager == null)
			_fileManager = WindowsXPFileManager.instance();
		return _fileManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.OperatingSystem#errorHandlerInstance()
	 */
	@Override
	public ErrorHandler errorHandlerInstance()
	{
		// TODO Auto-generated method stub
		return _errorHandler;
	}

	public JavaHome getJavaHome(Configuration config)
	{
		return new WindowsJavaHome(config);
	}

	@Override
	public ServiceManager serviceManagerInstance()
	{
		if (_serviceManager == null)
			_serviceManager = WindowsXPServiceManager.instance();
		return _serviceManager;
	}

	@Override
	public SystemInformation systemInformation()
	{
		return _systemInformation;
	}

	@Override
	public boolean setWorkingDir(String name)
	{
		return new WindowsXPProcess().changeWorkingDir(name);
	}

	@Override
	public Object getServiceFailureActions(Configuration config)
	{
		return WindowsXPService.getServiceFailureActions(config);
	}

	@Override
	public long getUptime()
	{
		return new WindowsXPProcess().getUptime();
	}
	
	@Override
	public void reboot()
	{
		try
		{
		 new WindowsXPProcess().reboot();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
