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

package org.rzo.yajsw.tray;

import org.rzo.yajsw.wrapper.AbstractWrappedProcessMBean;

// TODO: Auto-generated Javadoc
/**
 * The Interface WrapperTrayIcon.
 */
public interface WrapperTrayIcon
{

	/**
	 * Checks if is inits the.
	 * 
	 * @return true, if is inits the
	 */
	public boolean isInit();

	/**
	 * Info.
	 * 
	 * @param caption
	 *            the caption
	 * @param message
	 *            the message
	 */
	public void info(String caption, String message);

	/**
	 * Error.
	 * 
	 * @param caption
	 *            the caption
	 * @param message
	 *            the message
	 */
	public void error(String caption, String message);

	/**
	 * Warning.
	 * 
	 * @param caption
	 *            the caption
	 * @param message
	 *            the message
	 */
	public void warning(String caption, String message);

	/**
	 * Message.
	 * 
	 * @param caption
	 *            the caption
	 * @param message
	 *            the message
	 */
	public void message(String caption, String message);

	/**
	 * Sets the process.
	 * 
	 * @param proxy
	 *            the new process
	 */
	public void setProcess(AbstractWrappedProcessMBean proxy);

	/**
	 * Close console.
	 */
	public void closeConsole();

	/**
	 * Show state.
	 * 
	 * @param state
	 *            the state
	 */
	public void showState(int state);

	/**
	 * Checks if is stop.
	 * 
	 * @return true, if is stop
	 */
	public boolean isStop();

}
