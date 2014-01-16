package org.rzo.yajsw.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.vfs2.FileSystemException;
import org.rzo.yajsw.Constants;
import org.rzo.yajsw.config.YajswConfiguration;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.util.Utils;
import org.rzo.yajsw.util.VFSUtils;
import org.rzo.yajsw.wrapper.StateChangeListener;
import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedProcessFactory;
import org.rzo.yajsw.wrapper.WrappedRuntimeProcess;
import org.rzo.yajsw.os.Process;

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

		conf.setProperty("wrapper.control", "APPLICATION");
		conf.setProperty("wrapper.console.loglevel", "INFO");
		conf.setProperty("wrapper.console.format", "ZM");
		conf.setProperty("wrapper.logfile.loglevel", "NONE");
		if ("true".equals(System.getProperty("wrapper.runtime.java.default.shutdown", "false")))
			conf.setProperty("wrapper.on_exit.default", "SHUTDOWN");
		conf.setProperty("wrapper.console.pipestreams", "true");

		System.out.println(conf.getProperty("wrapper.console.pipestreams"));

		stopIfRunning(conf);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				if (_debug)
					System.err.println("ShutdownHook started");
				if (p.isOSProcessRunning())
				{
					if (_debug)
						System.err.println("runtime process warapper is shutting down, stopping runtime process");
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
				System.err.println("wrapped runtime process stopped with exit code " + exitCode);
				if (p.isOSProcessRunning())
					p.shutdown();
				System.exit(exitCode);
			}
				});

		p.start();

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
			if (pid != -1)
			{
				int shutdownWaitTime = conf.getInt("wrapper.shutdown.timeout",
						Constants.DEFAULT_SHUTDOWN_TIMEOUT) * 1000;
				stopProcess(shutdownWaitTime, pid);
			}
		}

	}

	private static void stopProcess(int timeout, int pid)
	{
		try
		{
			Process p = OperatingSystem.instance().processManagerInstance()
					.getProcess(pid);
			if (p != null)
			{
				if (_debug)
					System.out.println("stopping process with pid/timeout "
							+ pid + " " + timeout);
				p.stop(timeout, 999);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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

}
