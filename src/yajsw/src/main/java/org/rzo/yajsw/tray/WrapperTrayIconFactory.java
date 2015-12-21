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

package org.rzo.yajsw.tray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.rzo.yajsw.boot.WrapperLoader;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.Process;
import org.rzo.yajsw.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating WrapperTrayIcon objects.
 */
public class WrapperTrayIconFactory
{

	/**
	 * Creates a new WrapperTrayIcon object.
	 * 
	 * @param name
	 *            the name
	 * @param icon
	 *            the icon
	 * 
	 * @return the wrapper tray icon
	 */
	public static WrapperTrayIcon createTrayIcon(String name, String icon,
			YajswConfigurationImpl config)
	{
		WrapperTrayIcon result = null;
		if (config == null)
			config = new YajswConfigurationImpl();
		try
		{
			result = new WrapperTrayIconImpl(name, icon, config);
		}
		catch (Throwable ex)
		{
			System.out.println("java version does not support SystemTray: "
					+ ex.getMessage());
			ex.printStackTrace();
		}
		if (result == null || !result.isInit())
			result = new WrapperTrayIconDummy();
		return result;
	}

	/**
	 * Start tray icon process.
	 * 
	 * @param config
	 *            the config
	 * 
	 * @return the process
	 */
	public static Process startTrayIconProcess(YajswConfigurationImpl config,
			Logger logger)
	{
		if (config == null)
			return null;
		String wrapperConfFileName = config.getCachedPath(false);

		final Process osProcess = OperatingSystem.instance()
				.processManagerInstance().createProcess();

		try
		{
			List<String> cmd = new ArrayList<String>();
			cmd.add(getJava());
			for (Entry<String, String> e : config.getEnvLookupSet().entrySet())
			{
				String opt = Utils.getDOption(e.getKey(), e.getValue());
				if (!cmd.contains(opt))
					cmd.add(opt);
			}
			String tmpDir = config.getString("wrapper.tmp.path", null);
			if (tmpDir == null || tmpDir.startsWith("?"))
				tmpDir = System.getProperty("jna_tmpdir", null);
			if (tmpDir != null)
			{
				String opt = Utils.getDOption("jna_tmpdir", tmpDir);
				if (!cmd.contains(opt))
					cmd.add(opt);
			}
			else
			{
				tmpDir = System.getProperty("java.io.tmpdir", null);
				if (tmpDir != null)
				{
					String opt = Utils.getDOption("jna_tmpdir", tmpDir);
					if (!cmd.contains(opt))
						cmd.add(opt);
				}
			}

			cmd.add("-classpath");
			cmd.add(WrapperLoader.getWrapperJar());
			cmd.add(TrayIconMainBooter.class.getName());
			cmd.add(wrapperConfFileName);
			String[] arrCmd = new String[cmd.size()];
			for (int i = 0; i < arrCmd.length; i++)
				arrCmd[i] = (String) cmd.get(i);
			osProcess.setCommand(arrCmd);
			osProcess.setPipeStreams(false, false);
			osProcess.setVisible(false);
			osProcess.setLogger(logger);
			osProcess
					.setDebug(config.getBoolean("wrapper.debug", false) ? config
							.getInt("wrapper.debug.level", 3) > 1 : false);
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					if (osProcess != null)
						osProcess.kill(0);
				}
			});
			osProcess.start();
			int debug = config.getBoolean("wrapper.debug", false) ? config
					.getInt("wrapper.debug.level", 3) : 0;
			if (debug > 1)
				logger.info("spawned system tray icon process with pid "
						+ osProcess.getPid());
			return osProcess;
		}
		catch (Exception e)
		{
			logger.throwing("WRapperTRayIconFactory", "startTrayIconProcess", e);
		}
		return null;
	}

	/**
	 * Gets the java.
	 * 
	 * @return the java
	 */
	public static String getJava()
	{
		String result = System.getenv("java_exe");
		if (result == null)
		{
			result = System.getProperty("sun.boot.library.path");
			if (result != null)
				result = result + "/javaw";
			else
				result = "java";
		}
		return result;
	}

}
