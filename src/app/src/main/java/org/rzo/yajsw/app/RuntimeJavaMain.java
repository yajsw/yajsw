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
package org.rzo.yajsw.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.configuration2.Configuration;
import org.rzo.yajsw.Constants;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.wrapper.StateChangeListener;
import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedRuntimeProcess;

public class RuntimeJavaMain
{
	static boolean _stop = false;
	static boolean _debug = false;

	public static void main(String[] args)
	{
		_debug = "true".equals(System.getProperty("wrapper.debug", "false"));
		final WrappedRuntimeProcess p = new WrappedRuntimeProcess();
		Configuration conf = p.getConfiguration();
		clearKeys(conf, "wrapper.control");
		clearKeys(conf, "wrapper.console");
		clearKeys(conf, "wrapper.logfile");
		clearKeys(conf, "wrapper.on_exit");

		clearKeys(conf, "wrapper.filter");
		clearKeys(conf, "wrapper.tray");
		clearKeys(conf, "wrapper.image.javawrapper");
		clearKeys(conf, "wrapper.script");
		clearKeys(conf, "wrapper.restart.reload_configuration");
		clearKeys(conf, "wrapper.filter");
		clearKeys(conf, "wrapper.java");

		clearKeys(conf, "wrapper.console.visible");
		clearKeys(conf, "wrapper.jvm_exit.timeout");
		clearKeys(conf, "wrapper.service");

		conf.setProperty("wrapper.control", "APPLICATION");
		conf.setProperty("wrapper.console.loglevel", "INFO");
		conf.setProperty("wrapper.console.format", "ZM");
		conf.setProperty("wrapper.logfile.loglevel", "NONE");
		if ("true".equals(System.getProperty(
				"wrapper.runtime.java.default.shutdown", "false")))
			conf.setProperty("wrapper.on_exit.default", "SHUTDOWN");
		conf.setProperty("wrapper.console.pipestreams", "true");

		conf.setProperty("wrapper.console.visible", "true");
		conf.setProperty("wrapper.jvm_exit.timeout", 0);
		conf.setProperty("wrapper.service", false);

		System.out.println("pipe streams: "
				+ conf.getProperty("wrapper.console.pipestreams"));
		System.out.println("visible     : "
				+ conf.getProperty("wrapper.console.visible"));
		stopIfRunning(conf);

		// monitor application stop activity
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				System.err.println("Shutdown monitor started");

				while (p.isOSProcessRunning())
				{
					if (_debug)
					{
						System.err.println("waiting for termination....");
					}
					RuntimeJavaMain.sleep(5000);
				}
			}
		}));

		// stop the application
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				System.err
						.println("ShutdownHook started: stopping application");
				if (p.isOSProcessRunning())
				{
					if (_debug)
						System.err
								.println("runtime process warapper is shutting down, stopping runtime process");
					p.stop();
				}
			}
		}));

		p.addStateChangeListener(WrappedProcess.STATE_IDLE,
				new StateChangeListener()
				{

					public void stateChange(int newState, int oldState)
					{
						int exitCode = p.getExitCode();
						if (_debug)
							System.err
									.println("wrapped runtime process stopped with exit code "
											+ exitCode);
						if (p.isOSProcessRunning())
							p.shutdown();
						System.exit(exitCode);
					}
				});

		p.start();

		startTestThreadIfNeeded();

	}

	private static void stopIfRunning(Configuration conf)
	{
		int pid = -1;
		String file = conf.getString("wrapper.runtime.pidfile");
		if (file != null)
		{
			File f = new File(file);
			BufferedReader b = null;
			if (f.exists())
				try
				{
					b = new BufferedReader(new FileReader(f));
					pid = Integer.parseInt(b.readLine());
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			if (b != null)
				try
				{
					b.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			if (pid == -1)
				return;

			Process p = OperatingSystem.instance().processManagerInstance()
					.getProcess(pid);
			if (p == null)
				return;

			String cmd = p.getCommand();
			if (cmd == null)
				return;

			String image = conf.getString("wrapper.image", null);
			if (!cmd.contains(image))
				return;

			int timeout = conf.getInt("wrapper.shutdown.timeout",
					Constants.DEFAULT_SHUTDOWN_TIMEOUT) * 1000;
			System.out.println("process with pid " + pid + " and cmd " + cmd
					+ " is still running,stopping process with timeout of "
					+ timeout + "ms");
			sleep(3000);
			p.stop(timeout, 999);
			sleep(2000);
		}
	}

	private static void clearKeys(Configuration conf, String key)
	{
		if (conf.containsKey(key))
			conf.clearProperty(key);
		Iterator<String> keys = conf.getKeys(key);
		while (keys.hasNext())
			conf.clearProperty(keys.next());
	}

	private static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (Exception e)
		{
		}
	}

	private static void startTestThreadIfNeeded()
	{
		if (Boolean.getBoolean("StopAfter30Sec"))
		{
			new Thread()
			{
				public void run()
				{
					RuntimeJavaMain.sleep(30000);
					System.exit(0);
				}
			}.start();
		}
	}

}
