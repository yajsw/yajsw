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

package org.rzo.yajsw.os.ms.win.w32;

import io.netty.util.internal.logging.InternalLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import jnacontrib.win32.Registry;
import jnacontrib.win32.Registry.REGISTRY_ROOT_KEY;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.rzo.yajsw.boot.WrapperLoader;
import org.rzo.yajsw.os.JavaHome;

import com.sun.jna.Platform;

// TODO: Auto-generated Javadoc
/**
 * The Class JavaHome.
 */
public class WindowsJavaHome implements JavaHome
{

	/** The _config. */
	Configuration _config;
	InternalLogger _logger;
	int _debug = 3;

	public void setLogger(InternalLogger logger, int debug)
	{
		_logger = logger;
		_debug = debug;
	}

	/**
	 * Instantiates a new java home.
	 * 
	 * @param config
	 *            the config
	 */
	WindowsJavaHome(Configuration config)
	{
		_config = config;
	}

	/**
	 * Find java.
	 * 
	 * @return the string
	 */
	public String findJava(String wrapperJava, String customProcName)
	{
		String java = null;
		// String wrapperJava = _config.getString("wrapper.java.command");
		if (wrapperJava != null && !wrapperJava.endsWith(".exe")
				&& !wrapperJava.endsWith("}"))
			wrapperJava = wrapperJava + ".exe";
		try
		{
			// check if this is relative to wrapper home
			if (wrapperJava != null)
			{
				File f = new File(wrapperJava);
				if (!f.isAbsolute())
				{
					File f2 = new File(WrapperLoader.getWrapperHome(),
							wrapperJava);
					if (f2.exists())
						try
						{
							wrapperJava = f2.getCanonicalPath();
						}
						catch (IOException e)
						{
							_logger.warn("Exception in findJava()", e);
						}
				}
				else
				{
					wrapperJava = new File(wrapperJava).getCanonicalPath();
					if (!new File(wrapperJava).exists())
					{
						_logger.error("java file does not exist: "
								+ wrapperJava);
						return null;
					}
				}
			}
		}
		catch (Exception e)
		{
			_logger.warn("Error in JavaHome.findJava(): ", e);
		}
		java = wrapperJava;
		// String customProcName =
		// _config.getString("wrapper.java.customProcName");
		boolean useJavaw = _config.getBoolean("wrapper.java.command.javaw",
				(wrapperJava != null) && (wrapperJava.endsWith("javaw.exe")));

		if (java == null)
		{
			String minVersion = _config
					.getString("wrapper.java.command.minVersion");
			String maxVersion = _config
					.getString("wrapper.java.command.maxVersion");
			boolean b64bit = _config.getBoolean("wrapper.java.command.64bit",
					false);
			boolean jreOnly = _config.getBoolean(
					"wrapper.java.command.jreOnly", false);
			boolean preferJdk = _config.getBoolean(
					"wrapper.java.command.preferJdk", false);
			boolean preferJre = _config.getBoolean(
					"wrapper.java.command.preferJre", true && !preferJdk);
			boolean jdkOnly = _config.getBoolean(
					"wrapper.java.command.jdkOnly", false);
			String registryBase = Platform.is64Bit() && (!b64bit) ? "SOFTWARE\\Wow6432Node"
					: "SOFTWARE";
			if (!jdkOnly && (jreOnly || preferJre))
			{
				java = findJavaInRegistry(new String[] {
						registryBase + "\\JavaSoft\\Java Runtime Environment",
						registryBase + "\\IBM\\Java2 Runtime Environment" },
						minVersion, maxVersion, b64bit);
			}
			if (java == null && !jreOnly)
				java = findJavaInRegistry(new String[] {
						registryBase + "\\JavaSoft\\Java Development Kit",
						registryBase + "\\IBM\\Java Development Kit" },
						minVersion, maxVersion, b64bit);
		}
		else if (customProcName != null)
		{
			String h = java;
			File f = new File(h);
			if (f.exists())
				java = f.getParentFile().getParentFile().getAbsolutePath();
			else
				// if we have to copy java, we need to find the path to java
				java = findJavaFromPath(java);
		}
		else
			// user has given us wrapper.java and we need not rename the exe ->
			// return user input
			return java;

		// we have no java path -> abort
		if (java == null)
			return null;

		// java is now set to a path, it must exist
		File javaFile = new File(java);
		if (!javaFile.exists())
			return null;

		java = javaFile.getAbsolutePath() + "/bin";
		if (!useJavaw)
		{
			java += "/java.exe";
		}
		else
			java += "/javaw.exe";
		if (customProcName != null)
		{
			java = copyTotmp(java, customProcName);
		}

		if (_logger == null)
			System.out.println("using java: " + java);
		else if (_debug > 1)
			_logger.info("using java: " + java);

		return java;

	}

	/**
	 * Find java from path.
	 * 
	 * @param java
	 *            the java
	 * 
	 * @return the string
	 */
	private String findJavaFromPath(String java)
	{
		if (java != null)
		{
			File javaFile = new File(java);
			if (javaFile.exists())
				return java;
		}

		// search java in environment path
		String[] paths = System.getenv("path").split(";");
		for (String path : paths)
		{
			if (path.contains("jdk") || path.contains("jre"))
			{
				File javaFile = new File(path);
				if (javaFile.exists())
				{
					return path.substring(0, path.length() - 4);
				}
			}
		}
		return null;

	}

	/**
	 * Copy totmp.
	 * 
	 * @param java
	 *            the java
	 * @param customProcName
	 *            the custom proc name
	 * 
	 * @return the string
	 */
	private String copyTotmp(String java, String customProcName)
	{
		try
		{
			boolean isTmp = true;
			File javaFile = new File(java);
			File tmpJavaFile = null;
			try
			{
				File fc = new File(customProcName);
				if (fc.isAbsolute() && fc.getParentFile().exists())
				{
					tmpJavaFile = fc;
					isTmp = false;
				}
			}
			catch (Exception ex)
			{
				// ignore
			}

			// Create temp file.
			customProcName = customProcName.endsWith(".exe") ? customProcName
					.substring(0, customProcName.length() - 4) : customProcName;
			String exeName = "java_" + customProcName + "_";
			if (tmpJavaFile == null)
				try
				{
					tmpJavaFile = File.createTempFile(exeName, ".exe");
					copyFile(javaFile, tmpJavaFile);
				}
				catch (Exception ex)
				{
					_logger.error("error creating tmp file: " + exeName, ex);
				}
			// Delete temp file when program exits.
			if (!tmpJavaFile.exists())
				copyFile(javaFile, tmpJavaFile);
			if (isTmp)
				tmpJavaFile.deleteOnExit();
			return tmpJavaFile.getAbsolutePath();
		}
		catch (Throwable e)
		{
			_logger.error("error copying java: " + java + " -> "
					+ customProcName, e);
		}
		return null;
	}

	/**
	 * Copy file.
	 * 
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void copyFile(File in, File out) throws IOException
	{
		_logger.info("copying java: " + in.getAbsolutePath() + " -> "
				+ out.getAbsolutePath());
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try
		{
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	/**
	 * Find java from java home env.
	 * 
	 * @return the string
	 */
	private String findJavaFromJavaHomeEnv()
	{
		return System.getenv("JAVA_HOME");
	}

	/**
	 * Find java in registry.
	 * 
	 * @param keys
	 *            the keys
	 * @param minVersion
	 *            the min version
	 * @param maxVersion
	 *            the max version
	 * @param b64bit
	 *            the b64bit
	 * 
	 * @return the string
	 */
	private String findJavaInRegistry(String[] keys, String minVersion,
			String maxVersion, boolean b64bit)
	{
		String[] values = null;
		String result = null;
		String resultKey = null;
		String resultDir = null;
		minVersion = minVersion == null ? "1.1.0" : minVersion;
		maxVersion = maxVersion == null ? "99.99.99" : maxVersion;
		for (String key : keys)
		{
			try
			{
				values = Registry.getSubKeys(REGISTRY_ROOT_KEY.LOCAL_MACHINE,
						key);
				for (String value : values)
				{
					String dir = Registry.getStringValue(
							REGISTRY_ROOT_KEY.LOCAL_MACHINE,
							key + "\\" + value, "JavaHome");
					boolean exists = false;
					try
					{
						exists = dir != null && new File(dir).exists();
					}
					catch (Exception ex)
					{
						System.out.println("wrong registry key value: " + dir);
					}
					if (exists && value.compareTo(maxVersion) <= 0
							&& value.compareTo(minVersion) >= 0)
					{
						if (result == null)
						{
							result = value;
							resultKey = key;
							resultDir = dir;
						}
						else if (value.compareTo(result) >= 0)
						{
							result = value;
							resultKey = key;
							resultDir = dir;
						}
					}

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return resultDir;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
	{
		Configuration conf = new BaseConfiguration();
		conf.setProperty("wrapper.java.command", "java");
		WindowsJavaHome javaHome = new WindowsJavaHome(conf);
		System.out.println(javaHome.findJava(
				conf.getString("wrapper.java.command"),
				conf.getString("wrapper.java.command")));

		conf.setProperty("wrapper.java.customProcName", "test");
		javaHome = new WindowsJavaHome(conf);
		System.out.println(javaHome.findJava(
				conf.getString("wrapper.java.command"),
				conf.getString("wrapper.java.command")));

		conf.setProperty("wrapper.java.command", "javaw");
		javaHome = new WindowsJavaHome(conf);
		System.out.println(javaHome.findJava(
				conf.getString("wrapper.java.command"),
				conf.getString("wrapper.java.command")));

		conf.clear();
		conf.setProperty("wrapper.java.minversion", "1.5.0");
		conf.setProperty("wrapper.java.maxversion", "1.5.99");
		conf.setProperty("wrapper.java.customProcName", "test");
		javaHome = new WindowsJavaHome(conf);
		System.out.println(javaHome.findJava(
				conf.getString("wrapper.java.command"),
				conf.getString("wrapper.java.command")));

		conf.clear();
		conf.setProperty("wrapper.java.minversion", "1.6.0");
		conf.setProperty("wrapper.java.customProcName", "test");
		conf.setProperty("wrapper.java.preferJdk", true);
		javaHome = new WindowsJavaHome(conf);
		System.out.println(javaHome.findJava(
				conf.getString("wrapper.java.command"),
				conf.getString("wrapper.java.command")));

	}

}
