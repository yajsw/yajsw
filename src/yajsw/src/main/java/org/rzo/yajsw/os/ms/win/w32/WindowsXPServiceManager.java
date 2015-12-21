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

import java.util.Map;

import org.rzo.yajsw.os.Service;
import org.rzo.yajsw.os.ServiceInfo;
import org.rzo.yajsw.os.ServiceManager;

public class WindowsXPServiceManager implements ServiceManager
{

	/** The _instance. */
	static ServiceManager _instance;

	/**
	 * Instance.
	 * 
	 * @return the process manager
	 */
	public static ServiceManager instance()
	{
		if (_instance == null)
			_instance = new WindowsXPServiceManager();
		return _instance;
	}

	public Service createService()
	{
		return new WindowsXPService();
	}

	public Service getService(String name)
	{
		return WindowsXPService.getService(name);
	}

	public Map<String, ServiceInfo> getServiceList()
	{
		return WindowsXPService.getServiceList();
	}

	public ServiceInfo getServiceInfo(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
