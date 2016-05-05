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
package org.rzo.yajsw.os.posix.bsd;

import org.apache.commons.configuration2.Configuration;
import org.rzo.yajsw.os.ErrorHandler;
import org.rzo.yajsw.os.FileManager;
import org.rzo.yajsw.os.JavaHome;
import org.rzo.yajsw.os.Keyboard;
import org.rzo.yajsw.os.ProcessManager;
import org.rzo.yajsw.os.ServiceManager;
import org.rzo.yajsw.os.SystemInformation;
import org.rzo.yajsw.os.posix.OperatingSystemPosix;

public class OperatingSystemBSD extends OperatingSystemPosix
{

	private static ProcessManager _processManagerInstance;
	private static FileManager _fileManagerInstance;
	private static ServiceManager _serviceManagerInstance;
	private static SystemInformation _systemInformation = new BSDSystemInformation();

	@Override
	public ErrorHandler errorHandlerInstance()
	{
		return new BSDErrorHandler();
	}

	@Override
	public JavaHome getJavaHome(Configuration config)
	{
		return new BSDJavaHome(config);
	}

	@Override
	public Keyboard keyboardInstance()
	{
		return null;
	}

	@Override
	public ProcessManager processManagerInstance()
	{
		if (_processManagerInstance == null)
			_processManagerInstance = new BSDProcessManager();
		return _processManagerInstance;
	}

	@Override
	public ServiceManager serviceManagerInstance()
	{
		if (_serviceManagerInstance == null)
			_serviceManagerInstance = new BSDServiceManager();
		return _serviceManagerInstance;
	}

	@Override
	public SystemInformation systemInformation()
	{
		return _systemInformation;
	}

	@Override
	public FileManager fileManagerInstance()
	{
		if (_fileManagerInstance == null)
			_fileManagerInstance = new BSDFileManager();
		return _fileManagerInstance;
	}

	@Override
	public long getUptime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reboot()
	{
		throw new RuntimeException("not yet implemented");
	}

}
