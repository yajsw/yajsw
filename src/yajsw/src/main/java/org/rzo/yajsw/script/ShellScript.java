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

package org.rzo.yajsw.script;

import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class ShellScript.
 */
public class ShellScript extends AbstractScript
{
	volatile Process p = null;

	/**
	 * Instantiates a new shell script.
	 * 
	 * @param script
	 *            the script
	 * @param timeout
	 */
	public ShellScript(String script, String id, WrappedProcess process,
			String[] args, int timeout, int maxConcInvocations)
	{
		super("scripts/" + script, id, process, args, timeout,
				maxConcInvocations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.script.AbstractScript#execute(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.Object)
	 */
	public Object execute(String line)
	{
		String id = _id;
		String state = _process.getStringState();
		String count = "" + _process.getRestartCount();
		String pid = "" + _process.getAppPid();
		String exitCode = "" + _process.getExitCode();
		try
		{
			p = OperatingSystem.instance().processManagerInstance()
					.createProcess();
			p.setCommand(getScript() + " " + id + " " + state + " " + count
					+ " " + pid + " " + exitCode);
			p.setPipeStreams(false, false);
			p.start();
			p.waitFor(getTimeout());
			if (p.isRunning())
				p.kill(999);
			if (p.getExitCode() != 0)
				System.out.println("script " + getScript() + "returned "
						+ p.getExitCode());
			p.destroy();
			p = null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public Object execute()
	{
		return execute("");
	}

	public void executeWithTimeout()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void interrupt()
	{
		if (p != null)
		{
			p.destroy();
		}
	}

	void log(String msg)
	{
		if (_process != null && _process.getInternalWrapperLogger() != null)
			_process.getInternalWrapperLogger().info(msg);
		else
			System.out.println(msg);
	}

}
