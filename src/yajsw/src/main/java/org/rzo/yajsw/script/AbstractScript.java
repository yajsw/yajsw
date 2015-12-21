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

package org.rzo.yajsw.script;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.rzo.yajsw.util.DaemonThreadFactory;
import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractScript.
 */
public abstract class AbstractScript implements Script
{

	/** The _name. */
	String _name;

	/** The _timeout. */
	int _timeout = 30000;

	WrappedProcess _process;

	String _id;

	String[] _args;

	final static Timer TIMER = new HashedWheelTimer();
	static final ExecutorService EXECUTOR = (ThreadPoolExecutor) new ThreadPoolExecutor(
			0, 50, 120L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
			new DaemonThreadFactory("scriptExecutorInternal"));
	volatile Future _future;
	volatile Timeout _timerTimeout;

	AtomicInteger _remainingConcInvocations;

	/**
	 * Instantiates a new abstract script.
	 * 
	 * @param script
	 *            the script
	 * @param timeout
	 */
	public AbstractScript(String script, String id, WrappedProcess process,
			String[] args, int timeout, int maxConcInvocations)
	{
		_name = script;
		_process = process;
		_id = id;
		_args = args;
		if (timeout > 0)
			_timeout = timeout * 1000;
		_remainingConcInvocations = new AtomicInteger(maxConcInvocations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.script.Script#execute(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.Object)
	 */
	public abstract Object execute(String line);

	public abstract void interrupt();

	abstract void log(String msg);

	synchronized public void executeWithTimeout(final String line)
	{
		if (!checkRemainConc())
		{
			log("script: " + _name
					+ " : too many concurrent invocations -> abort execution");
			return;
		}
		Object result = null;
		_timerTimeout = TIMER.newTimeout(new TimerTask()
		{

			public void run(Timeout arg0) throws Exception
			{
				log("script takes too long -> interrupt");
				try
				{
					interrupt();
				}
				catch (Throwable e)
				{

				}
			}

		}, _timeout, TimeUnit.MILLISECONDS);
		_future = EXECUTOR.submit(new Callable<Object>()
		{
			public Object call()
			{
				Object result = execute(line);
				if (_timerTimeout != null)
					_timerTimeout.cancel();
				_timerTimeout = null;
				_remainingConcInvocations.incrementAndGet();
				log("executed script: " + _name + " "
						+ _remainingConcInvocations);
				return result;
			}
		});
		Thread.yield();
	}

	private boolean checkRemainConc()
	{
		System.out.println("checkRemainConc " + _name + " "
				+ _remainingConcInvocations);
		if (_remainingConcInvocations.decrementAndGet() < 0)
		{
			_remainingConcInvocations.incrementAndGet();
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.script.Script#getScript()
	 */
	public String getScript()
	{
		return _name;
	}

	/**
	 * Gets the timeout.
	 * 
	 * @return the timeout
	 */
	public int getTimeout()
	{
		return _timeout;
	}

	/**
	 * Sets the timeout.
	 * 
	 * @param timeout
	 *            the new timeout
	 */
	public void setTimeout(int timeout)
	{
		_timeout = timeout;
	}

	public String getId()
	{
		return _id;
	}

}
