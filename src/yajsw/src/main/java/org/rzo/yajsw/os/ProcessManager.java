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

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProcessManager.
 */
public interface ProcessManager
{

	/**
	 * Creates the process.
	 * 
	 * @return the process
	 */
	public Process createProcess();

	/**
	 * Gets the process.
	 * 
	 * @param pid
	 *            the pid
	 * 
	 * @return the process
	 */
	public Process getProcess(int pid);

	/**
	 * Current process id.
	 * 
	 * @return the int
	 */
	public int currentProcessId();

	/**
	 * Process id of active window.
	 * 
	 * @return the int
	 */
	public int processIdOfActiveWindow();

	/**
	 * Gets the process tree.
	 * 
	 * @param pid
	 *            the pid
	 * 
	 * @return the process tree
	 */
	public List getProcessTree(int pid);

	/**
	 * Task list instance.
	 * 
	 * @return the task list
	 */
	public TaskList taskListInstance();

	public List getProcessIds();

	public int umask(int mode);

}
