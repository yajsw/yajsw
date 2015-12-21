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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.os.ProcessManager;
import org.rzo.yajsw.os.TaskList;

// TODO: Auto-generated Javadoc
/**
 * The Class WindowsXPProcessManager.
 */
public class WindowsXPProcessManager implements ProcessManager
{

	/** The _instance. */
	static ProcessManager _instance;

	/**
	 * Instance.
	 * 
	 * @return the process manager
	 */
	public static ProcessManager instance()
	{
		if (_instance == null)
			_instance = new WindowsXPProcessManager();
		return _instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.ProcessManager#createProcess()
	 */
	public Process createProcess()
	{
		return new WindowsXPProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.ProcessManager#getProcess(int)
	 */
	public Process getProcess(int pid)
	{
		return WindowsXPProcess.getProcess(pid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.ProcessManager#currentProcessId()
	 */
	public int currentProcessId()
	{
		return WindowsXPProcess.currentProcessId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.ProcessManager#processIdOfActiveWindow()
	 */
	public int processIdOfActiveWindow()
	{
		return WindowsXPProcess.processIdOfActiveWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.ProcessManager#getProcessTree(int)
	 */
	public List getProcessTree(int pid)
	{
		return WindowsXPProcess.getProcessTree(pid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.ProcessManager#taskListInstance()
	 */
	public TaskList taskListInstance()
	{
		return WindowsXPTaskList.instance();
	}

	public List getProcessIds()
	{
		Map[] maps = WindowsXPProcess.getProcessMaps(0);
		if (maps[0].size() > 0)
		{
			System.out.println("getids " + maps[0].keySet().size());
			return new ArrayList(maps[0].keySet());
		}
		else
		{
			System.out.println("getids " + maps[1].keySet().size());
			return new ArrayList(maps[1].keySet());
		}

	}

	@Override
	public int umask(int mode)
	{
		// linux only
		return -1;
	}

}
