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

import java.util.ArrayList;

public class WrappedProcessList extends ArrayList<WrappedProcess>
{
	public void startAll()
	{
		for (WrappedProcess p : this)
		{
			p.start();
		}
	}

	public void stopAll(String reason)
	{
		for (WrappedProcess p : this)
		{
			p.stop(reason);
		}
	}

	public void onStopWrapper()
	{
		for (WrappedProcess p : this)
		{
			if (p.isHaltAppOnWrapper())
				p.stop();
		}
	}

	public void initAll()
	{
		for (WrappedProcess p : this)
		{
			p.init();
		}
	}

	public void restartAll()
	{
		for (WrappedProcess p : this)
		{
			p.restart();
		}
	}

	public void removeStateChangeListener(int state)
	{
		for (WrappedProcess p : this)
		{
			p.removeStateChangeListener(state);
		}
	}

	public void shutdown()
	{
		for (WrappedProcess p : this)
		{
			p.shutdown();
		}
	}

}
