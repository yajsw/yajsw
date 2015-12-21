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
package org.rzo.yajsw.timer;

import java.util.concurrent.ScheduledFuture;

import org.rzo.yajsw.timer.TimerImpl.MySimpleTrigger;
import org.rzo.yajsw.timer.TimerImpl.Trigger;

abstract public class Job implements Runnable
{
	ScheduledFuture _future;
	Trigger _trigger;
	volatile int _runCounter = 0;

	public boolean checkStart()
	{
		if (_trigger instanceof MySimpleTrigger)
		{
			MySimpleTrigger sTrigger = (MySimpleTrigger) _trigger;
			if (sTrigger._repeatCount > -1)
			{
				if (_runCounter++ > sTrigger._repeatCount)
				{
					if (_future != null)
						_future.cancel(false);
					return false;
				}
			}
		}
		return true;
	}

	public void setFuture(ScheduledFuture future)
	{
		_future = future;
	}

	public void setTrigger(Trigger trigger)
	{
		_trigger = trigger;
	}

}
