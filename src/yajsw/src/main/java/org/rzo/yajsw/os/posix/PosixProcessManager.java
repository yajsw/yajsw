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

import java.util.List;

import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.os.ProcessManager;
import org.rzo.yajsw.os.TaskList;

public class PosixProcessManager implements ProcessManager
{
	private static ProcessManager _instance;

	public static synchronized ProcessManager instance()
	{
		if (_instance == null)
			_instance = new PosixProcessManager();
		return _instance;
	}

	public Process createProcess()
	{
		return new PosixProcess();
	}

	public int currentProcessId()
	{
		return PosixProcess.currentProcessId();
	}

	public Process getProcess(int pid)
	{
		return PosixProcess.getProcess(pid);
	}

	public List getProcessTree(int pid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int processIdOfActiveWindow()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public TaskList taskListInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List getProcessIds()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int umask(int mode)
	{
		return PosixProcess.umask(mode);
	}

}
