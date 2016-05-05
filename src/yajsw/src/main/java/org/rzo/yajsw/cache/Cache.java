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
package org.rzo.yajsw.cache;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.rzo.yajsw.Constants;
import org.rzo.yajsw.config.YajswConfigurationImpl;
import org.rzo.yajsw.util.VFSUtils;

public class Cache
{
	boolean _loaded = false;
	Logger _logger;
	int _debug;

	public Cache(Logger logger, int debug)
	{
		_logger = logger;
		_debug = debug;
	}

	public Cache()
	{
		this(null, 0);
	}

	public boolean load(YajswConfigurationImpl config)
	{
		try
		{
			// reload if not yet loaded, or wrapper.restart.reload_configuration
			// is true
			boolean reload = config.getBoolean(
					"wrapper.restart.reload_configuration",
					Constants.DEFAULT_RELOAD_CONFIGURATION);
			if (!reload && _loaded)
				return true;
			String workingDir = config.getString("wrapper.working.dir", ".");
			String base = config.getString("wrapper.codebase", workingDir);
			String cache = config.getCache();

			DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS
					.getManager();

			FileObject basef = VFSUtils.resolveFile(".", base);
			FileObject cachef = VFSUtils.resolveFile(".", cache);
			if (!VFSUtils.isLocal(cachef))
			{
				log("cache must be a local folder -> abort");
				return false;
			}
			if (!cachef.exists())
				cachef.createFolder();
			if (cachef.getType() != FileType.FOLDER)
			{
				log("cache must be a folder -> abort");
				return false;
			}
			boolean cacheLocal = config.getBoolean("wrapper.cache.local",
					Constants.DFAULT_CACHE_LOCAL);

			Configuration resources = config.subset("wrapper.resource");
			for (Iterator it = resources.getKeys(); it.hasNext();)
			{
				String key = (String) it.next();
				loadFiles("wrapper.resource." + key, config, basef, cachef,
						cacheLocal, fsManager);
			}

			Configuration classpath = config.subset("wrapper.java.classpath");
			for (Iterator it = classpath.getKeys(); it.hasNext();)
			{
				String key = (String) it.next();
				loadFiles("wrapper.java.classpath." + key, config, basef,
						cachef, cacheLocal, fsManager);
			}

			_loaded = true;
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}

	private void loadFiles(String key, YajswConfigurationImpl config,
			FileObject basef, FileObject cachef, boolean cacheLocal,
			DefaultFileSystemManager fsManager)
	{
		try
		{
			String value = config.getString(key);
			List files = VFSUtils.resolveFiles(basef, value, fsManager);
			int count = 0;
			for (Iterator it = files.iterator(); it.hasNext();)
			{
				FileObject source = (FileObject) it.next();
				FileObject destination = loadFile(value, source, basef, cachef,
						cacheLocal, fsManager);
				if (destination != null)
				{
					config.getFileConfiguration().setProperty(
							key + "$" + count++,
							destination.getURL().getFile().substring(2));
					// System.out.println("set configuration "+key+" "+destination.getURL().getFile());
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private FileObject loadFile(String value, FileObject source,
			FileObject basef, FileObject cachef, boolean cacheLocal,
			DefaultFileSystemManager fsManager)
	{
		try
		{
			boolean isLocal = VFSUtils.isLocal(source);
			if (isLocal && !cacheLocal)
				return null;
			FileObject destination = null;
			boolean absolute = false;
			// assume value is absolute
			try
			{
				destination = VFSUtils.resolveFile((String) null, value);
				absolute = true;
			}
			catch (Exception ex)
			{
			}
			if (!absolute)
				destination = VFSUtils.resolveFile(cachef, basef.getName()
						.getRelativeName(source.getName()));
			else
			{
				String fileName = destination.getName().getBaseName();
				destination = VFSUtils.resolveFile(cachef, fileName);
			}
			if (fileChanged(source, destination))
			{
				destination.copyFrom(source, new AllFileSelector());
				if (source.getContent().getLastModifiedTime() != 0)
					destination.getContent().setLastModifiedTime(
							source.getContent().getLastModifiedTime());
				if (_debug > 1)
					log("cache file loaded " + source.getName() + " -> "
							+ destination.getName());
			}
			else if (_debug > 2)
				log("cache file up to date: " + destination.getName());
			return destination;
		}
		catch (Exception ex)
		{
			log("error in cache.loadFile: "
					+ (source == null ? "null" : source.getName()));
			ex.printStackTrace();
		}
		return null;
	}

	private boolean fileChanged(FileObject source, FileObject destination)
	{
		try
		{
			return !destination.exists()
					|| source.getContent().getLastModifiedTime() == 0
					|| source.getContent().getLastModifiedTime() != destination
							.getContent().getLastModifiedTime();
		}
		catch (FileSystemException e)
		{
			e.printStackTrace();
			return true;
		}
	}

	private void log(String msg)
	{
		if (_logger == null)
			System.out.println(msg);
		else
			_logger.info(msg);
	}

	public static void main(String[] args)
	{
		Cache c = new Cache();
		System.setProperty("wrapper.config",
				"http://localhost:8080/wrapper.helloworld.conf");
		YajswConfigurationImpl config = new YajswConfigurationImpl();
		c.load(config);

	}

}
