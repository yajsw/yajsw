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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.rzo.yacron4j.Scheduler;
import org.rzo.yacron4j.SchedulerOptions;
import org.rzo.yacron4j.TaskOptions;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class Timer.
 */
public class TimerImpl implements Timer
{

	private static final int REPEAT_INDEFINITELY = -1;

	/** The _config. */
	YajswConfigurationImpl _config;

	/** The _wp. */
	WrappedProcess _wp;

	/** The _scheduler. */
	Scheduler _cronScheduler;
	ScheduledExecutorService _simpleScheduler;

	/** The _cron start. */
	MyCronTrigger _cronStart;

	/** The _cron stop. */
	MyCronTrigger _cronStop;

	/** The _cron restart. */
	MyCronTrigger _cronRestart;

	/** The _simple start. */
	MySimpleTrigger _simpleStart;

	/** The _simple stop. */
	MySimpleTrigger _simpleStop;

	/** The _simple restart. */
	MySimpleTrigger _simpleRestart;

	/** The _has trigger. */
	boolean _hasTrigger = false;

	/** The _start immediate. */
	boolean _startImmediate = true;

	/** The _triggered. */
	boolean _triggered = false;

	/**
	 * Instantiates a new timer.
	 * 
	 * @param config
	 *            the config
	 * @param wp
	 *            the wp
	 */
	public TimerImpl(YajswConfigurationImpl config, WrappedProcess wp)
	{
		_config = config;
		_wp = wp;
	}

	/**
	 * Inits the.
	 */
	public synchronized void init()
	{
		for (Iterator keys = _config.getKeys("wrapper.timer"); keys.hasNext();)
		{
			String key = (String) keys.next();
			if (key.contains(".simple."))
			{
				if (key.contains(".START."))
				{
					if (_simpleStart == null)
						_simpleStart = getSimpleTrigger(key);
				}
				else if (key.contains(".STOP."))
				{
					if (_simpleStop == null)
						_simpleStop = getSimpleTrigger(key);
				}
				else if (key.contains(".RESTART."))
				{
					if (_simpleRestart == null)
						_simpleRestart = getSimpleTrigger(key);
				}
				else
					System.out.println("Cannot interpret timer property: "
							+ key);
			}
			else if (key.contains(".cron."))
			{
				if (key.contains(".START"))
					_cronStart = getCronTrigger(key);
				else if (key.contains(".STOP"))
					_cronStop = getCronTrigger(key);
				else if (key.contains(".RESTART"))
					_cronRestart = getCronTrigger(key);
				else
					System.out.println("Cannot interpret timer property: "
							+ key);
			}
			else
			{
				System.out.println("Cannot interpret timer property: " + key);
			}
		}

	}

	/**
	 * Gets the simple trigger.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the simple trigger
	 */
	private MySimpleTrigger getSimpleTrigger(String key)
	{
		Job jobDetail = getJobDetail(key);
		if (jobDetail == null)
			return null;
		Date startTime = getStartTime(key);
		int repeatCount = getRepeatCount(key);
		int interval = getInterval(key);

		MySimpleTrigger trigger;
		try
		{
			trigger = new MySimpleTrigger(jobDetail, startTime, repeatCount,
					interval);
		}
		catch (Exception ex)
		{
			return null;
		}

		_hasTrigger = true;
		_startImmediate = false; // getStartTime will always return a date.
		// per default the current time.
		jobDetail.setTrigger(trigger);
		return trigger;
	}

	/**
	 * Gets the interval.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the interval
	 */
	private int getInterval(String key)
	{
		int x = _config.getInt(key.substring(0, key.lastIndexOf("."))
				+ ".INTERVAL", REPEAT_INDEFINITELY);
		if (x > 0)
			x = x * 1000;
		return x;
	}

	/**
	 * Gets the repeat count.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the repeat count
	 */
	private int getRepeatCount(String key)
	{
		return _config.getInt(
				key.substring(0, key.lastIndexOf(".")) + ".COUNT",
				REPEAT_INDEFINITELY);
	}

	/**
	 * Gets the start time.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the start time
	 */
	private Date getStartTime(String key)
	{
		String str = _config.getString(key.substring(0, key.lastIndexOf("."))
				+ ".FIRST");
		if (str == null)
			return new Date();
		SimpleDateFormat df = null;
		if (str.contains(" "))
			df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		else
			df = new SimpleDateFormat("HH:mm:ss");
		try
		{
			return df.parse(str);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the job class.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the job class
	 */
	private Job getJobDetail(String key)
	{
		if (key.contains(".RESTART"))
			return new RestartJob(_wp);
		else if (key.contains(".STOP"))
			return new StopJob(_wp);
		else if (key.contains(".START"))
			return new StartJob(_wp);
		return null;
	}

	/**
	 * Gets the cron trigger.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the cron trigger
	 */
	private MyCronTrigger getCronTrigger(String key)
	{
		Job job = getJobDetail(key);
		if (job == null)
			return null;

		String cronExpression = getCronExpression(key);
		if (cronExpression != null)
		{
			MyCronTrigger trigger = new MyCronTrigger(cronExpression, job);
			if (job instanceof StartJob)
				_startImmediate = false;
			_hasTrigger = true;
			return trigger;
		}
		return null;
	}

	/**
	 * Gets the cron expression.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the cron expression
	 */
	private String getCronExpression(String key)
	{
		String str = _config.getString(key);
		if (str == null)
		{
			return null;
		}
		return str;
	}

	/**
	 * Start.
	 */
	public synchronized void start()
	{
		if (!_hasTrigger)
			return;
		if (_cronStart != null)
			startTrigger(_cronStart, _cronStart.getJobDetail());
		if (_cronStop != null)
			startTrigger(_cronStop, _cronStop.getJobDetail());
		if (_cronRestart != null)
			startTrigger(_cronRestart, _cronRestart.getJobDetail());
		if (_simpleStart != null)
			startTrigger(_simpleStart, _simpleStart.getJobDetail());
		if (_simpleStop != null)
			startTrigger(_simpleStop, _simpleStop.getJobDetail());
		if (_simpleRestart != null)
			startTrigger(_simpleRestart, _simpleRestart.getJobDetail());
		_triggered = true;
	}

	static int tcount = 0;

	/**
	 * Gets the scheduler.
	 * 
	 * @return the scheduler
	 */
	private ScheduledExecutorService getSimpleScheduler()
	{
		if (_simpleScheduler == null)
		{
			_simpleScheduler = Executors.newSingleThreadScheduledExecutor();
		}
		return _simpleScheduler;
	}

	/**
	 * Start trigger.
	 * 
	 * @param trigger
	 *            the trigger
	 * @param jobDetail
	 *            the job detail
	 */
	private void startTrigger(Trigger trigger, Job jobDetail)
	{
		if (trigger == null || jobDetail == null)
			return;
		if (trigger instanceof MyCronTrigger)
			try
			{
				TaskOptions taskOptions = new TaskOptions();
				String cronExpression = getCronExpression(trigger);
				TimeZone timeZone = getTimeZone(trigger);
				if (timeZone != null)
					taskOptions.setTimeZone(timeZone);
				getCronScheduler().schedule(jobDetail,
						cronExpression, taskOptions);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		else
		{
			MySimpleTrigger simpleTrigger = (MySimpleTrigger) trigger;
			ScheduledExecutorService scheduler = getSimpleScheduler();
			long initialDelay = 0;
			ScheduledFuture future;
			if (simpleTrigger._startTime != null)
			{
				initialDelay = simpleTrigger._startTime.getTime()
						- System.currentTimeMillis();
				if (initialDelay < 0)
					initialDelay = 0;
			}
			if (simpleTrigger._interval > 0)
			{
				future = scheduler.scheduleWithFixedDelay(jobDetail,
						initialDelay, simpleTrigger._interval,
						TimeUnit.MILLISECONDS);
				jobDetail.setFuture(future);

			}
			else
			{
				future = scheduler.schedule(jobDetail, initialDelay,
						TimeUnit.MILLISECONDS);
				jobDetail.setFuture(future);
			}

		}
	}

	private TimeZone getTimeZone(Trigger trigger) {
		String result = ((MyCronTrigger) trigger)._cronExpression;
		int i = result.indexOf('@');
		if (i != -1)
		{
			result = result.substring(i+1);
			TimeZone tz = TimeZone.getTimeZone(result.trim());
			if (!tz.getID().equals(result) && !tz.getDisplayName().equals(result))
				throw new RuntimeException("timezone not found: "+result);
			return tz;
		}
		return null;
	}

	private String getCronExpression(Trigger trigger) {
		String result = ((MyCronTrigger) trigger)._cronExpression;
		int i = result.indexOf('@');
		if (i != -1)
			result = result.substring(0, i);
		return result;
			
		
	}

	private Scheduler getCronScheduler()
	{
		if (_cronScheduler == null)
		{
			try
			{
				SchedulerOptions options = new SchedulerOptions();
				options.setDebug(_config.getBoolean("wrapper.debug", false));
				options.setMaxThreads(1);
				options.setThreadFactory(new ThreadFactory()
				{

					@Override
					public Thread newThread(Runnable r)
					{
						Thread result = new Thread(r);
						result.setDaemon(true);
						result.setName("yajsw-timer-" + tcount++);
						return result;
					}
				});
				_cronScheduler = new Scheduler(options);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_cronScheduler = null;
			}
		}
		return _cronScheduler;
	}

	/**
	 * Stop.
	 */
	public void stop()
	{
		if (!_hasTrigger)
			return;
		stopTrigger(_cronStart);
		stopTrigger(_cronStop);
		stopTrigger(_cronRestart);
		stopTrigger(_simpleStart);
		stopTrigger(_simpleStop);
		stopTrigger(_simpleRestart);

	}

	/**
	 * Stop trigger.
	 * 
	 * @param trigger
	 *            the trigger
	 */
	private synchronized void stopTrigger(Trigger trigger)
	{
		try
		{
			_cronScheduler.shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		_triggered = false;
	}

	/**
	 * Checks if is triggered.
	 * 
	 * @return true, if is triggered
	 */
	public boolean isTriggered()
	{
		return _triggered;
	}

	/**
	 * Checks if is start immediate.
	 * 
	 * @return true, if is start immediate
	 */
	public boolean isStartImmediate()
	{
		return _startImmediate;
	}

	/**
	 * Checks if is checks for trigger.
	 * 
	 * @return true, if is checks for trigger
	 */
	public boolean isHasTrigger()
	{
		return _hasTrigger;
	}

	interface Trigger
	{
		Runnable getJobDetail();
	}

	class AbstractTrigger implements Trigger
	{
		final Job _job;

		AbstractTrigger(Job job)
		{
			_job = job;
		}

		@Override
		public Job getJobDetail()
		{
			return _job;
		}
	}

	/**
	 * The Class MyCronTrigger.
	 */
	class MyCronTrigger extends AbstractTrigger
	{

		String _cronExpression;

		MyCronTrigger(String cronExpression, Job job)
		{
			super(job);
			_cronExpression = cronExpression;
		}

	}

	/**
	 * The Class MySimpleTrigger.
	 */
	class MySimpleTrigger extends AbstractTrigger
	{
		Date _startTime;
		int _repeatCount;
		int _interval;

		/**
		 * Instantiates a new my simple trigger.
		 * 
		 * @param interval
		 * @param repeatCount
		 * @param startTime
		 * 
		 * @param jobDetail
		 *            the job detail
		 */
		MySimpleTrigger(Job job, Date startTime, int repeatCount, int interval)
		{
			super(job);
			if (startTime == null && repeatCount == REPEAT_INDEFINITELY
					&& interval == REPEAT_INDEFINITELY)
				throw new RuntimeException("simple trigger configuration error");
			_startTime = startTime;
			_repeatCount = repeatCount;
			_interval = interval;
		}

	}

}
