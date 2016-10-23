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

package org.rzo.yajsw.config;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.io.FileOptionsProvider;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.configuration2.io.VFSFileSystem;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.rzo.yajsw.config.jnlp.JnlpSupport;
import org.rzo.yajsw.os.OperatingSystem;
import org.rzo.yajsw.script.GroovyScript;
import org.rzo.yajsw.util.CaseInsensitiveMap;
import org.rzo.yajsw.util.CommonsLoggingAdapter;
import org.rzo.yajsw.util.ConfigurationLoggingAdapter;
import org.rzo.yajsw.util.VFSUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class AjswConfigurationImpl.
 */
public class YajswConfigurationImpl extends CompositeConfiguration implements
		YajswConfiguration
{

	/** The log. */
	InternalLogger log = InternalLoggerFactory.getInstance(this.getClass()
			.getName());

	/** The _system properties. */
	Configuration _systemProperties;

	/** The _local configuration. */
	Configuration _localConfiguration;

	/** The _system configuration. */
	CompositeConfiguration _systemConfiguration = new CompositeConfiguration();

	/** The debug. */
	boolean debug = false;

	/** The _use system properties. */
	boolean _useSystemProperties = true;

	boolean _isStopper = false;

	boolean _init = false;

	FilePropertiesConfiguration _fileConfiguration = null;

	YajswConfigurationInterpolator _interpolator;

	Set _interpolated = new HashSet();

	Map _scriptUtils = null;

	boolean _isJavaDebug = false;

	/**
	 * Instantiates a new ajsw configuration impl.
	 */
	public YajswConfigurationImpl()
	{
		init();
	}

	/**
	 * Instantiates a new ajsw configuration impl.
	 * 
	 * @param debug
	 *            the debug
	 */
	public YajswConfigurationImpl(boolean debug)
	{
		setDebug(debug);
		init();
	}

	public YajswConfigurationImpl(Configuration localConfiguration,
			boolean useSystemProperties)
	{
		this(localConfiguration, useSystemProperties, null);
	}

	/**
	 * Instantiates a new ajsw configuration impl.
	 * 
	 * @param localConfiguration
	 *            the local configuration
	 * @param useSystemProperties
	 *            the use system properties
	 */
	public YajswConfigurationImpl(Configuration localConfiguration,
			boolean useSystemProperties, Map scriptUtils)
	{
		_localConfiguration = localConfiguration;
		_useSystemProperties = useSystemProperties;
		_scriptUtils = scriptUtils;
		init();
	}

	public YajswConfigurationInterpolator getYajswInterpolator()
	{
		return _interpolator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.AjswConfiguration#init()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init()
	{
		if (_init)
			return;
		/*
		 * ConfigurationInterpolator in = (ConfigurationInterpolator)
		 * getSubstitutor().getVariableResolver(); StrLookup orgLookup =
		 * in.getDefaultLookup(); in.setDefaultLookup(new
		 * MyStrLookup(orgLookup));
		 */
		
		this.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		_systemConfiguration.setListDelimiterHandler(new DefaultListDelimiterHandler(','));

		
		if (_scriptUtils == null)
			_scriptUtils = new HashMap();
		try
		{
			// use introspection, so that groovy can be removed from the
			// classpath, if not required
			_interpolator = createGInterpolatornew(this, true, null,
					_scriptUtils);
			this.setInterpolator((ConfigurationInterpolator) _interpolator);
		}
		catch (Exception e1)
		{
			// e1.printStackTrace();
			log.warn("WARNING: could not instatiate groovy in wrapper configuration.");

		}

		if (_localConfiguration != null)
		{
			_systemConfiguration.addConfiguration(_localConfiguration);
			if (debug)
				log.debug("added local configuration ");
		}
		// order of adding configurations to composite is important
		// first added hides the others

		// load configuration from System Properties
		if (_useSystemProperties)
		{
			_systemProperties = ConfigurationConverter
					.getConfiguration((Properties) System.getProperties()
							.clone());
			
			_systemConfiguration.addConfiguration(_systemProperties);
			if (debug)
				log.debug("added system configuration ");
		}
		// _systemConfiguration.addConfiguration(new
		// EnvironmentConfiguration());
		_systemConfiguration.addConfiguration(new MapConfiguration(
				!OperatingSystem.instance().isPosix() ? new CaseInsensitiveMap(
						System.getenv()) : new HashMap(System.getenv())));

		addConfiguration(_systemConfiguration);
		// check if we have config file
		String configFile = (String) getProperty("wrapper.config");
		if (configFile != null && configFile.contains("\""))
			configFile = configFile.replaceAll("\"", "");

		// load configuration from file
		if (configFile == null)
		{
			if (debug)
				log.warn("configuration file not set");
		}
		else if (!fileExists(configFile))
			log.error("configuration file not found: " + configFile);
		else
		{
			// check if we have a jnlp file
			if (configFile.endsWith(".jnlp"))
				try
				{
					JnlpSupport jnlp = new JnlpSupport(configFile);
					_fileConfiguration = jnlp
							.toConfiguration((String) getProperty("wrapperx.default.config"));
					_fileConfiguration.setFileName(configFile);
					addConfiguration(_fileConfiguration);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return;
				}
			// else try a standard configuration
			if (_fileConfiguration == null)
				try
				{
					// enable VFS
					FileSystem fs = new VFSFileSystem();
					fs.setLogger(new ConfigurationLoggingAdapter(log));
					fs.setFileOptionsProvider(new FileOptionsProvider()
					{

						public Map getOptions()
						{
							Map result = new HashMap();
							String httpProxy = System
									.getProperty("http.proxyHost");
							String httpPort = System
									.getProperty("http.proxyPort");
							if (httpProxy != null)
							{
								int port = 8080;
								if (httpPort != null)
									try
									{
										port = Integer.parseInt(httpPort);
									}
									catch (Exception ex)
									{
										ex.printStackTrace();
									}
								result.put(FileOptionsProvider.PROXY_HOST,
										httpProxy);
								result.put(FileOptionsProvider.PROXY_PORT, port);
							}
							return result;

						}

					});
					// TODO  !!!! FileSystem.setDefaultFileSystem(fs);
					// allow for conditional incldues -> first createn an empty
					// properties conf
					_fileConfiguration = new FilePropertiesConfiguration();
					// then set the file name and load it
					_fileConfiguration.setFileName(configFile);
					/*
					 * try { _fileConfiguration.setBasePath(new
					 * File(".").getCanonicalPath()); } catch (IOException e) {
					 * // TODO Auto-generated catch block e.printStackTrace(); }
					 */

					_fileConfiguration.append(_systemConfiguration);
					if (_interpolator != null)
						try
						{
							_fileConfiguration
									.setInterpolator((ConfigurationInterpolator) createGInterpolatornew(
											_fileConfiguration, true, null,
											_scriptUtils));
						}
						catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					// System.out.println("platform "+_systemConfiguration.getString("platform"));
						_fileConfiguration.load();
					String encoding = _fileConfiguration
							.getString("wrapper.conf.encoding");
					// if we have an encoding: reload the file with the given
					// encoding.
					if (encoding != null)
					{
						_fileConfiguration = new FilePropertiesConfiguration();
						_fileConfiguration.setEncoding(encoding);
						// then set the file name and load it
						_fileConfiguration.setFileName(configFile);
						/*
						 * try { _fileConfiguration.setBasePath(new
						 * File(".").getCanonicalPath()); } catch (IOException
						 * e) { // TODO Auto-generated catch block
						 * e.printStackTrace(); }
						 */
						_fileConfiguration.append(_systemConfiguration);
						if (_interpolator != null)
							try
							{
								_fileConfiguration
										.setInterpolator((ConfigurationInterpolator) createGInterpolatornew(
												_fileConfiguration, true, null,
												_scriptUtils));
							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							_fileConfiguration.load();
					}

					addConfiguration(_fileConfiguration);
				}
				catch (Exception e)
				{
					log.error(
							"error loading configuration file <init>AsjwConfiguration",
							e);
				}
			if (!isLocalFile())
			{
				// if no working dir is defined: set working dir to the cache
				if (_fileConfiguration.getProperty("wrapper.working.dir") == null)
					try
					{
						_fileConfiguration.setProperty("wrapper.working.dir",
								new File(getCache()).getCanonicalPath()
										.replaceAll("\\\\", "/"));
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				// if no cache path is defined in the file configuration then
				// set it, so it can be accessed by the wrapper for example to
				// get
				// a system tray icon
				if (_fileConfiguration.containsKey("wrapper.cache"))
					_fileConfiguration.setProperty("wrapper.cache", getCache());
			}

		}

		// load configuration from System Environement
		// addConfiguration(getConfiguration(System.getenv()));
		// _systemConfiguration.addConfiguration(new
		// EnvironmentConfiguration());
		_isStopper = this.getBoolean("wrapper.stopper", false);
		try
		{
			_isJavaDebug = this.getInt("wrapper.java.debug.port", -1) != -1;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		for (Iterator it = getKeys("wrapper.config.script"); it.hasNext();)
			try
			{
				String key = (String) it.next();
				String script = getString(key);
				String bind = key.substring(key.lastIndexOf(".") + 1);
				_scriptUtils.put(bind, new GroovyScript(script, "", null, null,
						0, log, null, false, 1));
			}
			catch (Exception e)
			{
				log.error("error reading script", e);
			}

		_init = true;
	}

	private YajswConfigurationInterpolator createGInterpolatornew(Configuration conf, boolean b,
			String[] object, Map utils)
	{
		YajswConfigurationInterpolator result = null;
		try
		{
			Class clazz = conf
					.getClass()
					.getClassLoader()
					.loadClass("org.rzo.yajsw.config.groovy.GInterpolator");
			Constructor rc = clazz.getDeclaredConstructor(Configuration.class,
					boolean.class, String[].class, Map.class);
			result = (YajswConfigurationInterpolator) rc.newInstance(conf, b, object, utils);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			log.warn("WARNING: could not load configuration groovy interpolator");
		}
		return result;
	}

	private boolean fileExists(String file)
	{
		try
		{
			// this hack is no longer required, changed VFS to init without
			// providers.xm.
			// String current =
			// System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
			// System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
			// "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
			DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS
					.getManager();
			// if (current != null)
			// System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
			// current);
			// else
			// System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
			FileObject f = VFSUtils.resolveFile(".", file);
			return f.exists();
		}
		catch (FileSystemException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks if is debug.
	 * 
	 * @return true, if is debug
	 */
	boolean isDebug()
	{
		return debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.AjswConfiguration#setDebug(boolean)
	 */
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	protected Object getPropertyInternal(String key)
	{
		Object result = null;
		if (key == null)
			result = null;
		if (_isJavaDebug)
		{
			if (key.equals("wrapper.startup.timeout"))
				result = Integer.MAX_VALUE / 1000;
			else if (key.equals("wrapper.shutdown.timeout"))
				result = (Integer.MAX_VALUE / 1000);
			else if (key.equals("wrapper.ping.timeout"))
				result = (Integer.MAX_VALUE / 1000);
		}
		if (result != null)
			return result;
		if (!_isStopper)
			result = super.getPropertyInternal(key);
		else if (key.startsWith("wrapper.on_exit"))
			return null;
		else if (key.startsWith("wrapper.exit_on_main_terminate"))
			result = "0";
		else if (key.startsWith("wrapper.daemon"))
			result = null;
		else if (key.contains(".script"))
			result = null;
		else if (key.contains(".filter"))
			result = null;
		else if (key.contains(".pidfile"))
			result = null;
		// allow stopper to have its own logging
		// else if (key.contains(".log"))
		// return null;
		else if (key.contains(".ntservice"))
			result = null;
		else if (key.contains(".jmx"))
			result = null;
		else if (key.contains(".lockfile"))
			result = null;
		else if (key.contains(".stop.conf"))
			result = null;
		else if (key.equals("wrapper.tray"))
			result = null;
		else
			result = super.getPropertyInternal(key);

		if (_interpolator != null && result != null
				&& !result.equals(_interpolator.interpolate(result)))
			_interpolated.add(key);
		return result;
	}

	public Set getLookupSet()
	{
		return _interpolated;
	}

	public Map<String, String> getEnvLookupSet()
	{
		if (_interpolator != null)
		{
			Map<String, String> result = _interpolator.getUsedEnvVars();
			result.putAll(_interpolator.getFromBinding());
			return result;
		}
		return new HashMap<String, String>();
	}

	public static void main(String[] args)
	{
		YajswConfigurationImpl c = new YajswConfigurationImpl();
		c.setProperty("t1", "x");
		c.setProperty("t2", "${t1}");
		System.out.println(c.getString("t2"));
		for (Iterator it = c.getInterpolator().prefixSet().iterator(); it
				.hasNext();)
			System.out.println(it.next());
	}

	public CompositeConfiguration getSystemConfiguration()
	{
		return _systemConfiguration;
	}

	public void reload() throws Exception
	{
		if (_fileConfiguration != null)
			_fileConfiguration.reload();
	}

	public boolean isLocalFile()
	{
		if (_fileConfiguration == null)
			return true;
		try
		{
			String name = _fileConfiguration.getURL();
			if (name.endsWith(".jnlp"))
				return false;

			FileObject f = VFSUtils.resolveFile(".", name);
			// if we set cache to CacheStrategy.ON_CALL FileObjects are
			// decorated to call refresh.
			// getContent().getFile() returns the decorated file
			return VFSUtils.isLocal(f);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return true;
		}

	}

	public String getCache()
	{
		return getString("wrapper.cache", "yajsw_cache");
	}

	public String getCachedPath()
	{
		return getCachedPath(true);
	}

	public String getCachedPath(boolean save)
	{
		if (_fileConfiguration == null)
			return null;
		if (isLocalFile())
			try {
				return new File(_fileConfiguration.getPath()).getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		try
		{
			String cache = getCache() + "/conf";
			String fileName = _fileConfiguration.getFileName();
			File cf = new File(cache);
			if (!cf.exists())
				cf.mkdirs();
			if (fileName.endsWith(".jnlp"))
				fileName = fileName + ".conf";
			// configuration file in the cache
			File cn = new File(cf, fileName);

			if (save)
			{
				// interpolate the file so that no includes are required
				FilePropertiesConfiguration c2 = _fileConfiguration
						.interpolatedFileConfiguration();

				// save the file
				c2.save(cn);
			}
			return cn.getCanonicalPath();

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return _fileConfiguration.getFileName();
		}

	}

	public Configuration getFileConfiguration()
	{
		// TODO Auto-generated method stub
		return _fileConfiguration;
	}

	public String getString(String key)
	{
		String value = super.getString(key);
		/*
		 * changed upon request, but should we really do this ? cross platform
		 * Portability of configurations ? if (value != null &&
		 * !key.contains("account") && !key.contains("wrapper.image") &&
		 * !key.contains("wrapper.app.env") &&
		 * !key.contains("wrapper.java.monitor.gc")) value =
		 * value.replaceAll("\\\\", "/");
		 */
		return value;
	}

	public long getConfigFileTime()
	{
		// TODO Auto-generated method stub
		return -1;
	}

	public boolean isStopper()
	{
		return _isStopper;
	}

}
