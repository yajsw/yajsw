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
package org.rzo.yajsw.condition;

import io.netty.util.internal.logging.InternalLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.script.Script;
import org.rzo.yajsw.script.ScriptFactory;
import org.rzo.yajsw.wrapper.WrappedProcess;

public class Condition
{
	Script _script;

	/** The _config. */
	YajswConfigurationImpl _config;

	/** The _wp. */
	WrappedProcess _wp;

	/** The _has trigger. */
	boolean _hasTrigger = false;

	/** The _triggered. */
	boolean _triggered = false;

	long _period = -1;

	static Timer _timer = new Timer("yajsw.condition");

	InternalLogger _log;

	int _debug = 3;

	public Condition(YajswConfigurationImpl config,
			WrappedProcess wrappedProcess, InternalLogger log)
	{
		_config = config;
		_wp = wrappedProcess;
		_log = log;
	}

	public void init()
	{
		String fileName = _config.getString("wrapper.condition.script");
		if (fileName == null)
			return;
		File f = new File(fileName);
		if (!f.exists() || !f.isFile())
		{
			try
			{
				System.out
						.println("file not found -> ignoring condition script "
								+ f.getCanonicalPath());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		int debugLevel = _config.getInt("wrapper.debug.level", 3);
		String dbg = _config.getString("wrapper.debug", null);
		_debug = (dbg != null && "true".equals(dbg)) ? debugLevel : 0;
		List args = _config.getList("wrapper.condition.script.args",
				new ArrayList());
		String[] argsArr = new String[args.size()];
		for (int i = 0; i < argsArr.length; i++)
			argsArr[i] = args.get(i).toString();
		_script = ScriptFactory.createScript(fileName, "condition", _wp,
				argsArr, _log, 0, _config.getString("wrapper.script.encoding"),
				_config.getBoolean("wrapper.script.reload", false), _debug, 1);
		_hasTrigger = _script != null;
		_period = _config.getLong("wrapper.condition.cycle", -1) * 1000;
	}

	public void stop()
	{
		_timer.cancel();
		_triggered = false;
	}

	public boolean isHasTrigger()
	{
		return _hasTrigger;
	}

	public boolean isTriggered()
	{
		return _triggered;
	}

	public void start()
	{
		_triggered = true;
		if (_period > 0)
			_timer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{
					_script.execute();
				}

			}, new Date(), _period);
		else
			_timer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{
					_script.execute();
				}

			}, new Date());

	}

}
