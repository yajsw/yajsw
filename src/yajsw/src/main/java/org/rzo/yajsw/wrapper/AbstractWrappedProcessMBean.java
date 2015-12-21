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
package org.rzo.yajsw.wrapper;

import java.awt.Color;
import java.util.Date;
import java.util.List;

public interface AbstractWrappedProcessMBean
{
	/**
	 * Start.
	 */
	public void start();

	/**
	 * Stop.
	 */
	public void stop();

	public void stop(String reason);

	/**
	 * Restart.
	 */
	public void restart();

	/**
	 * Gets the pid.
	 * 
	 * @return the pid
	 */
	public int getAppPid();

	/**
	 * Gets the exit code.
	 * 
	 * @return the exit code
	 */
	public int getExitCode();

	public String getStringState();

	public void threadDump();

	public void gc();

	public void wrapperThreadDump();

	public String getType();

	public String getName();

	public void waitFor();

	public void stopTimerCondition();

	public boolean isTimerActive();

	public boolean isConditionActive();

	public int getTotalRestartCount();

	public int getRestartCount();

	public Date getAppStarted();

	public Date getAppStopped();

	public int getWrapperPid();

	public Date getWrapperStarted();

	public int getAppCpu();

	public int getAppHandles();

	public long getAppVMemory();

	public long getAppPMemory();

	public int getAppThreads();

	public void startDrain();

	public List<String> readDrainLine();

	public void stopDrain();

	public int getState();

	public String[][] getTrayIconMessages();

	public void stopWrapper();

	public boolean hasOutput();

	public void writeOutput(String txt);

	public void writeInquireResponse(String s);

	public String getInquireMessage();

	public void init();

	public void setProperty(String key, String value);

	public void resetCache();

	public boolean isAppReportedReady();

	public void dumpHeap(String s);

	public Color getUserTrayColor();

	public void update(String updateConfFile);

}
