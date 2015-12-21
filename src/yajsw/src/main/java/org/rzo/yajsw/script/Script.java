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

import org.rzo.yajsw.wrapper.TriggerAction;

// TODO: Auto-generated Javadoc
/**
 * The Interface Script.
 */
public interface Script extends TriggerAction
{

	/**
	 * Execute.
	 * 
	 * @param id
	 *            the id
	 * @param state
	 *            the state
	 * @param count
	 *            the count
	 * @param pid
	 *            the pid
	 * @param exitCode
	 *            the exit code
	 * @param line
	 *            the line
	 * @param wrapperJavaProcess
	 *            the wrapper java process
	 */
	public Object execute();

	public Object execute(String line);

	/**
	 * Gets the script.
	 * 
	 * @return the script
	 */
	public String getScript();

	public void executeWithTimeout();

	public void executeWithTimeout(String line);
}
