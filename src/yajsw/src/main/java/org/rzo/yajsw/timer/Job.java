package org.rzo.yajsw.timer;

import java.util.concurrent.ScheduledFuture;

import org.rzo.yajsw.timer.TimerImpl.MySimpleTrigger;
import org.rzo.yajsw.timer.TimerImpl.Trigger;

abstract public class Job implements Runnable
{
	ScheduledFuture _future;
	Trigger         _trigger;
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
