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
package org.rzo.yajsw.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Cycler
{
	Executor _executor;
	long _period;
	long _delay;
	Runnable _runnable;
	boolean _started = false;
	boolean _terminated = true;
	Runnable _worker;
	Thread _cyclerThread = null;

	public Cycler(long period, long delay, Executor executor, Runnable runnable)
	{
		if (runnable == null || executor == null)
			throw new NullPointerException();
		_period = checkDuration(period, 100);
		_delay = checkDuration(delay, 0);
		_executor = executor;
		_runnable = runnable;
		_worker = new Runnable()
		{
			public void run()
			{
				_cyclerThread = Thread.currentThread();
				_terminated = false;
				if (_started)
					try
					{
						Thread.sleep(_delay);
					}
					catch (InterruptedException ex)
					{
						// interrupted -> continue
					}
				while (_started)
				{
					if (_started)
						try
						{
							_runnable.run();
						}
						catch (Throwable ex)
						{
							ex.printStackTrace();
						}
					if (_started)
						try
						{
							Thread.sleep(_period);
						}
						catch (InterruptedException e)
						{
							// interrupted -> continue
						}

				}
				_terminated = true;
				_cyclerThread = null;
			}
		};
	}

	public synchronized void start()
	{
		if (_started)
			return;
		_started = true;
		if (_terminated)
			_executor.execute(_worker);
	}

	public synchronized void stop()
	{
		_started = false;
		if (_cyclerThread != null)
			_cyclerThread.interrupt();
	}

	private long checkDuration(long duration, long minValue)
	{
		long result = duration;
		if (duration % 100 != 0)
			result = (duration / 100) * 100;
		if (result <= minValue)
			result = minValue;
		return result;
	}

	public static void main(String[] args) throws InterruptedException
	{
		Cycler c = new Cycler(1000, 0,
				Executors.newCachedThreadPool(new DaemonThreadFactory(
						"controller")), new Runnable()
				{
					public void run()
					{
						System.out.println(System.currentTimeMillis()
								+ " running");
					}
				});
		while (true)
		{
			c.start();
			Thread.sleep(1500);
			c.stop();
			System.out.println(System.currentTimeMillis() + " stopped");
		}
	}

}
