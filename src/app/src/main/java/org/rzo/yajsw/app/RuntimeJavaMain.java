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
	public static void main(String[] args)
	{
		/*
		try
		{
			final Process p = Runtime.getRuntime().exec(args);
			waitFor(p);
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
			{
				public void run()
				{
					if (_stop)
						return;
					p.destroy();
					_stop = true;
					System.exit(p.exitValue());
				}
			}));
			startInGobbler(p.getErrorStream());
			startInGobbler(p.getInputStream());
			startOutGobbler(p.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void waitFor(final Process p)
	{
		new Thread(new Runnable()
		{
			
			public void run()
			{
				try
				{
					p.waitFor();
					_stop = true;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				try
				{
				System.exit(p.exitValue());
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}).start();
	}

	private static void startOutGobbler(final OutputStream out)
	{
		new Thread(new Runnable()
		{

			public void run()
			{
				try
				{
					byte[] buffer = new byte[1024];
					for (int s = System.in.read(buffer); s != -1 && !_stop; s = System.in
							.read(buffer))
					{
						
						if (s == 0)
							Thread.sleep(100);
						else
							out.write(buffer, 0, s);
					}
				}
				catch (Exception ex)
				{
					System.err.println(ex.getMessage());
				}

			}
		}).start();
	}

	private static void startInGobbler(final InputStream in)
	{
		new Thread(new Runnable()
		{

			public void run()
			{
				try
				{
					byte[] buffer = new byte[1024];
					for (int s = in.read(buffer); s != -1 && !_stop; s = in.read(buffer))
					{
						
						if (s == 0)
							Thread.sleep(100);
						else
							System.out.write(buffer, 0, s);
					}
				}
				catch (Exception ex)
				{
					System.err.println(ex.getMessage());
				}

			}
		}).start();
		*/
		final WrappedRuntimeProcess p = new WrappedRuntimeProcess();
		Configuration conf = p.getConfiguration();
		clearKeys(conf, "wrapper.filter");
		clearKeys(conf, "wrapper.tray");
		clearKeys(conf, "wrapper.image.javawrapper");
		clearKeys(conf, "wrapper.logfile");
		clearKeys(conf, "wrapper.script");
		clearKeys(conf, "wrapper.console.pipestreams");

		conf.setProperty("wrapper.control", "APPLICATION");
		conf.setProperty("wrapper.console.loglevel", "INFO");
		conf.setProperty("wrapper.logfile.loglevel", "NONE");
		//conf.setProperty("wrapper.on_exit.default", "SHUTDOWN");
		conf.setProperty("wrapper.console.pipestreams", true);
		
		System.out.println(""+conf.getBoolean("wrapper.tray", false));
		
		stopIfRunning(conf);
		
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				p.stop();
			}
		}));
		
		p.addStateChangeListener(WrappedProcess.STATE_IDLE, new StateChangeListener()
		{
			
			public void stateChange(int newState, int oldState)
			{
				p.shutdown();
				System.exit(p.getExitCode());
			}
		});
		
		p.start();
		
	}
	
	private static void stopIfRunning(Configuration conf)
	{
		String file = conf.getString("wrapper.runtime.pidfile");
		if (file != null)
		{
			File f = new File(file);
			BufferedReader b = null;
			int pid = -1;
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
				stopProcess(pid);
			}
		}

	}
	private static void stopProcess(int pid)
	{
		try
		{
		Process p = OperatingSystem.instance().processManagerInstance().getProcess(pid);
		if (p != null)
			p.kill(999);
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
