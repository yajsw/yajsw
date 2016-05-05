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

import io.netty.util.internal.logging.SimpleLogger;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.tray.ahessian.client.AHessianJmxClient;
import org.rzo.yajsw.util.Utils;
import org.rzo.yajsw.wrapper.AbstractWrappedProcessMBean;
import org.rzo.yajsw.wrapper.TrayIconProxy.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class TrayIconMain.
 */
public class TrayIconMain
{

	/** The jmxc. */
	volatile static MBeanServerConnection jmxc = null;

	/** The url. */
	static JMXServiceURL url = null;

	/** The user. */
	static String user = null;

	/** The password. */
	static String password = null;

	/** The proxy. */
	volatile static AbstractWrappedProcessMBean proxy = null;

	/** The o name. */
	static ObjectName oName = null;

	/** The _tray icon. */
	static WrapperTrayIconImpl _trayIcon = null;

	/** The lock. */
	static FileLock lock = null;

	/** The _ahessian client. */
	static AHessianJmxClient _ahessianClient;

	private static String getName(Configuration _config)
	{
		String result = "";
		if (_config == null)
			return result;
		if (_config.getBoolean("wrapper.service", false))
			result += "Service ";
		String name = _config.getString("wrapper.console.title");
		if (name == null)
			name = _config.getString("wrapper.ntservice.name");
		if (name == null)
			name = _config.getString("wrapper.image");
		if (name == null)
			name = _config.getString("wrapper.groovy");
		if (name == null)
			name = _config.getString("wrapper.java.app.mainclass");
		if (name == null)
			name = _config.getString("wrapper.java.app.jar");
		if (name == null)
			name = "";
		result += name;
		return result;

	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception
	{
		String config = null;
		if (args.length > 0)
			config = args[0];
		File f = new File(config);
		if (!f.exists())
		{
			System.out.println("file not found " + f);
			config = null;
		}

		String canonName = null;
		if (config != null)
		{
			canonName = new File(config).getCanonicalPath();
			String tmpDir = System.getProperty("jna_tmpdir",
					System.getProperty("java.io.tmpdir"));
			File lockFile = new File(tmpDir + "/yajsw" + canonName.hashCode()
					+ ".lck");
			// System.out.println("system tray lock file: " +
			// lockFile.getCanonicalPath());
			FileChannel channel = new RandomAccessFile(lockFile, "rw")
					.getChannel();
			// Try acquiring the lock without blocking. This method returns
			// null or throws an exception if the file is already locked.
			try
			{
				lock = channel.tryLock();
			}
			catch (OverlappingFileLockException e)
			{
				// File is already locked in this thread or virtual machine
				return;
			}
			if (lock == null)
				return;
		}

		System.setProperty("wrapper.config", config);

		Configuration localConf = new BaseConfiguration();
		if (config != null)
			localConf.addProperty("wrapper.config", config);
		YajswConfigurationImpl _config = new YajswConfigurationImpl(localConf,
				true);

		try
		{
			String name = _config.getString("wrapper.console.title");
			if (name == null)
				name = _config.getString("wrapper.ntservice.name");
			if (name == null)
				name = "yajsw.noname";
			name = ObjectName.quote(name);
			oName = new ObjectName("org.rzo.yajsw", "name", name);
			_trayIcon = (WrapperTrayIconImpl) WrapperTrayIconFactory
					.createTrayIcon(getName(_config),
							_config.getString("wrapper.tray.icon"), _config);
			Utils.verifyIPv4IsPreferred(null);
			_ahessianClient = new AHessianJmxClient(canonName, _config.getInt(
					"wrapper.tray.port", 0), true, new SimpleLogger());
			_ahessianClient.setConnectListener(new Runnable()
			{

				@Override
				public void run()
				{
					if (_trayIcon.isStop())
						return;
					// TODO disableFunctions();

					try
					{
						jmxc = _ahessianClient.getMBeanServer();
						if (jmxc != null)
						{
							proxy = (AbstractWrappedProcessMBean) MBeanServerInvocationHandler
									.newProxyInstance(jmxc, oName,
											AbstractWrappedProcessMBean.class,
											false);
							_trayIcon.setProcess(proxy);
							_ahessianClient.open();
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}

			});
			_ahessianClient.setDisconnectListener(new Runnable()
			{

				@Override
				public void run()
				{
					jmxc = null;
					proxy = null;
					try
					{
						_trayIcon.closeConsole();
						_trayIcon.setProcess(null);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}

			});
			_ahessianClient.start();
			while (!_trayIcon.isStop())
			{
				if (jmxc != null && proxy != null)
					try
					{
						if (!showInquire(proxy.getInquireMessage()))
						{
							showMessages(proxy.getTrayIconMessages());
							_trayIcon.showState(proxy.getState());
							_trayIcon.showColor(proxy.getUserTrayColor());
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();

						System.out.println("error accessing server " + ex);
					}
				// System.out.println(">> "+proxy);
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			return;
		}
		Runtime.getRuntime().halt(0);
	}

	private static boolean showInquire(String message)
	{
		if (message == null)
			return false;
		if (_trayIcon == null)
			return false;
		if (_trayIcon._inquireMessage == null)
		{
			_trayIcon.message("Input Required", message
					+ "\n enter data through response menue");
			_trayIcon._inquireMessage = message;
			return true;
		}
		return true;

	}

	private static void showMessages(String[][] messages)
	{
		if (_trayIcon == null)
			return;
		if (messages == null)
			return;
		for (String[] message : messages)
		{
			Types type = Types.valueOf(message[0]);
			switch (type)
			{
			case ERROR:
				_trayIcon.error(message[1], message[2]);
				break;
			case INFO:
				_trayIcon.info(message[1], message[2]);
				break;
			case MESSAGE:
				_trayIcon.message(message[1], message[2]);
				break;
			case WARNING:
				_trayIcon.warning(message[1], message[2]);
				break;
			default:
				System.out.println("wrong message type");
			}
		}
	}

}
