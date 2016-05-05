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

import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;;

abstract public class AbstractService implements Service
{
	protected String _displayName;
	protected String _description;
	protected String[] _dependencies;
	protected String _account;
	protected String _password;
	protected String _command[];
	protected String _name;
	protected Configuration _config;
	protected String _startType;
	protected boolean _interactive;
	protected Logger _logger;
	protected Object _failureActions = null;
	protected String[] _stopDependencies;

	public String getDisplayName()
	{
		return _displayName;
	}

	public void setLogger(Logger logger)
	{
		_logger = logger;
	}

	public void setDisplayName(String displayName)
	{
		_displayName = displayName;
	}

	public String getDescription()
	{
		return _description;
	}

	public void setDescription(String description)
	{
		_description = description;
	}

	public String[] getDependencies()
	{
		return _dependencies;
	}

	public void setDependencies(String[] dependencies)
	{
		_dependencies = dependencies;
	}

	public String getAccount()
	{
		return _account;
	}

	public void setAccount(String account)
	{
		_account = account;
	}

	public String getPassword()
	{
		return _password;
	}

	public void setPassword(String password)
	{
		_password = password;
	}

	public String[] getCommand()
	{
		return _command;
	}

	public void setCommand(String[] command)
	{
		_command = command;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public Configuration getConfig()
	{
		return _config;
	}

	public void setConfig(Configuration config)
	{
		_config = config;
	}

	public boolean isAutomatic(int state)
	{
		return (state & STATE_AUTOMATIC) != 0;
	}

	public boolean isDisabled(int state)
	{
		return (state & STATE_DISABLED) != 0;
	}

	public boolean isInstalled(int state)
	{
		return (state & STATE_INSTALLED) != 0;
	}

	public boolean isInteractive(int state)
	{
		return (state & STATE_INTERACTIVE) != 0;
	}

	public boolean isManual(int state)
	{
		return (state & STATE_MANUAL) != 0;
	}

	public boolean isPaused(int state)
	{
		return (state & STATE_PAUSED) != 0;
	}

	public boolean isRunning(int state)
	{
		return (state & STATE_RUNNING) != 0;
	}

	public boolean isStarting(int state)
	{
		return (state & STATE_STARTING) != 0;
	}

	public boolean isStateUnknown(int state)
	{
		return (state & STATE_UNKNOWN) != 0;
	}

	public String getStartType()
	{
		return _startType;
	}

	public void setStartType(String startType)
	{
		_startType = startType;
	}

	public boolean isInteractive()
	{
		return _interactive;
	}

	public void setInteractive(boolean interactive)
	{
		_interactive = interactive;
	}

	public void setFailureActions(Object failureActions)
	{
		_failureActions = failureActions;
	}

	public Object getFailureActions()
	{
		return _failureActions;
	}

	public String[] getStopDependencies()
	{
		return _stopDependencies;
	}

	public void setStopDependencies(String[] stopDependencies)
	{
		_stopDependencies = stopDependencies;
	}

}
