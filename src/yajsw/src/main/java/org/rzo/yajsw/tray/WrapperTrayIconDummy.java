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
 * The Class WrapperTrayIconDummy.
 */
public class WrapperTrayIconDummy implements WrapperTrayIcon
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#isInit()
	 */
	public boolean isInit()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#error(java.lang.String,
	 * java.lang.String)
	 */
	public void error(String caption, String message)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#info(java.lang.String,
	 * java.lang.String)
	 */
	public void info(String caption, String message)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#message(java.lang.String,
	 * java.lang.String)
	 */
	public void message(String caption, String message)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#warning(java.lang.String,
	 * java.lang.String)
	 */
	public void warning(String caption, String message)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rzo.yajsw.tray.WrapperTrayIcon#setProcess(org.rzo.yajsw.wrapper.
	 * AbstractWrappedProcessMBean)
	 */
	public void setProcess(AbstractWrappedProcessMBean proxy)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#closeConsole()
	 */
	public void closeConsole()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#showState(int)
	 */
	public void showState(int state)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.tray.WrapperTrayIcon#isStop()
	 */
	public boolean isStop()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
