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
package org.rzo.yajsw.os.posix.bsd.macosx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.rzo.yajsw.Constants;
import org.rzo.yajsw.boot.WrapperLoader;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.os.AbstractService;
import org.rzo.yajsw.os.JavaHome;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.os.posix.PosixUtils;
import org.rzo.yajsw.os.posix.VelocityLog;
import org.rzo.yajsw.util.Utils;

public class MacOsXService extends AbstractService implements Constants
{
	String _launchdDir;
	String _plistTemplate;
	String _plistFile;
	int _stopTimeout;
	String _plistName;

	String _execCmd;

	String _confFile;
	PosixUtils _utils = new PosixUtils();

	public void init()
	{
		if (_name == null)
		{
			System.out.println("no name for daemon -> abort");
			return;
		}
		_launchdDir = _config.getString("wrapper.launchd.dir",
				getDefaultLaunchDir());
		File daemonDir = new File(_launchdDir);
		if (!daemonDir.exists())
		{
			System.out.println("No LaunchAgents directory found yet. Creating");
			daemonDir.mkdirs();
		}
		if (!daemonDir.isDirectory())
		{
			System.out
					.println("Error " + _launchdDir + " : is not a directory");
			return;
		}
		String wrapperJar = WrapperLoader.getWrapperJar().trim();
		String wrapperHome = ".";
		try
		{
			wrapperHome = new File(wrapperJar).getParentFile()
					.getCanonicalPath();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		String confFile = _config.getString("wrapper.config");
		String confDir = null;
		if (confFile != null)
		{
			File f = new File(confFile);
			if (f.exists())
				try
				{
					confDir = f.getParentFile().getCanonicalPath();
				}
				catch (IOException e)
				{
				}
		}
		if (confDir == null)
			confDir = wrapperHome + "/conf";
		if (confFile == null)
		{
			System.out.println("no conf file found -> abort");
			return;
		}
		try
		{
			_confFile = new File(confFile).getCanonicalPath();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		_plistTemplate = _config.getString("wrapper.launchd.template",
				wrapperHome + "/templates/launchd.plist.vm");
		File daemonTemplate = new File(_plistTemplate);
		if (!daemonTemplate.exists() || !daemonTemplate.isFile())
		{
			System.out.println("Error " + _plistTemplate
					+ " : template file not found");
			return;
		}
		File daemonScript = new File(daemonDir, "wrapper." + getName());
		if (daemonScript.exists())
			System.out.println(daemonScript.getAbsolutePath()
					+ " already exists -> overwrite");

		_plistName = getPlistPrefix() + _name;
		File plistFile = new File(_launchdDir, _plistName + ".plist");
		try
		{
			_plistFile = plistFile.getCanonicalPath();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		JavaHome javaHome = OperatingSystem.instance().getJavaHome(_config);
		
		String java = _config.getString("wrapper.ntservice.java.command");
		if (java == null)
			java = System.getProperty("java.home") + "/bin/java";
		try
		{
			java = new File(java).getCanonicalPath();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		String tmpDir = _config.getString("wrapper.tmp.path",
				System.getProperty("jna_tmpdir", null));
		ArrayList<String> result = new ArrayList<String>();
		String opt = null;
		if (tmpDir != null)
		{
			opt = Utils.getDOption("jna_tmpdir", tmpDir);
			result.add(opt);
		}
		YajswConfigurationImpl config = (YajswConfigurationImpl) _config;
		for (Iterator it = config.subset("wrapper").getKeys(); it.hasNext();)
			try
			{
				config.getProperty((String) it.next());
			}
			catch (Exception ex)
			{

			}
		// first add lookup vars eg ${lookup}
		if (_config.getBoolean("wrapper.save_interpolated", true))
		{

			for (Map.Entry<String, String> e : config.getEnvLookupSet()
					.entrySet())
			{
				if (e.getKey().contains("password"))
					continue;
				opt = Utils.getDOption(e.getKey(), e.getValue());
				if (!result.contains(opt))
					result.add(opt);
			}
		}

		for (Iterator it = _config.getKeys("wrapper.ntservice.additional"); it
				.hasNext();)
		{
			String key = (String) it.next();
			String value = _config.getString(key);
			result.add(value);
		}

		String properties = StringUtils.join(result, " ");

		_execCmd = String
				.format("\"%1$s\" -Dwrapper.service=true -Dwrapper.visible=false %2$s -jar \"%3$s\" -c \"%4$s\"",
						java, properties, wrapperJar, _confFile);

	}

	protected String getPlistPrefix()
	{
		return "wrapper.";
	}

	protected String getDefaultLaunchDir()
	{
		return System.getProperty("user.home") + "/Library/LaunchAgents";
	}

	public boolean install()
	{
		if (_plistFile == null)
		{
			System.out.println("Error : not initialized -> abort");
			return false;
		}
		try
		{
			File daemonTemplate = new File(_plistTemplate);
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
			ve.setProperty("file.resource.loader.path",
					daemonTemplate.getParent());
			ve.setProperty("runtime.log.logsystem.class",
					VelocityLog.class.getCanonicalName());
			ve.init();
			Template t = ve.getTemplate(daemonTemplate.getName());
			VelocityContext context = new VelocityContext();
			context.put("name", _plistName);
			context.put("command", splitCommandByWhitespace());
			context.put("autoStart", "AUTO_START".equals(_config.getString(
					"wrapper.ntservice.starttype", DEFAULT_SERVICE_START_TYPE)));
			FileWriter writer = new FileWriter(_plistFile);

			t.merge(context, writer);
			writer.flush();
			writer.close();
			preload();
			System.out.println(_utils.osCommand("launchctl load " + _plistFile,
					5000));

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		boolean result = isInstalled();
		int i = 0;
		while (!result && i < 10)
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return result;
			}
			result = isInstalled();
			i++;
		}
		return result;
	}

	protected void preload()
	{
		try
		{
		System.out.println(_utils.osCommand("chmod 644 " + _plistFile, 5000));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private List<String> splitCommandByWhitespace()
	{
		List<String> list = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*")
				.matcher(_execCmd);
		while (m.find())
			list.add(m.group(1).replace("\"", ""));
		return list;
	}

	public boolean isInstalled()
	{
		// String sp = String.format(".*\\d+.*%1$s.*", _plistName);
		// Pattern p = Pattern.compile(sp, Pattern.DOTALL);
		// String sp = String.format("^[(\\d+),-].*\\s*%1$s$", _plistName);
		// Pattern p = Pattern.compile(sp, Pattern.MULTILINE);
		String list = _utils.osCommand("launchctl list", 5000);
		// Matcher m = p.matcher(list);
		// return m.matches();
		return list.contains(_plistName);
	}

	public boolean isRunning()
	{
		int pid = getPid();
		return pid > 0;
	}

	public boolean start()
	{
		if (isRunning())
		{
			System.out.println("already running");
			return true;
		}
		_utils.osCommand("launchctl start " + _plistName, 5000);
		boolean result = isRunning();
		int i = 0;
		while (!result && i < 10)
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return result;
			}
			result = isRunning();
			i++;
		}
		return result;
	}

	public boolean stop()
	{
		if (isRunning())
		{
			_utils.osCommand("launchctl stop " + _plistName, 5000);
			boolean result = !isRunning();
			int i = 0;
			while (!result && i < 10)
			{
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					return result;
				}
				result = !isRunning();
				i++;
			}
			return result;
		}
		return true;
	}

	public boolean uninstall()
	{
		if (isRunning())
			stop();
		_utils.osCommand("launchctl unload " + _plistFile, 5000);
		new File(_plistFile).delete();
		boolean result = isInstalled();
		int i = 0;
		while (result && i < 10)
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return result;
			}
			result = isInstalled();
			i++;
		}
		return !result;
	}

	public int state()
	{
		int result = 0;
		if (isInstalled())
			result |= STATE_INSTALLED;
		if (isRunning())
			result |= STATE_RUNNING;
		return result;
	}

	public int getPid()
	{
		try
		{
			String sp = String.format("^(\\d+).*\\s*%1$s$", _plistName);
			Pattern p = Pattern.compile(sp, Pattern.MULTILINE);
			String list = _utils.osCommand("launchctl list", 5000);
			Matcher m = p.matcher(list);
			m.find();
			int pid = Integer.parseInt(m.group(1));
			return pid;
		}
		catch (Exception ex)
		{
			// ex.printStackTrace();
		}

		return -1;

	}

	public void setLogger(Logger logger)
	{
		super.setLogger(logger);
		_utils.setLog(logger);
	}

}
