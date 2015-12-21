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

import org.rzo.yajsw.os.FileManager;
import org.rzo.yajsw.util.File;

public class WindowsXPFileManager implements FileManager
{

	/** The _instance. */
	static FileManager _instance;

	/**
	 * Instance.
	 * 
	 * @return the process manager
	 */
	public static FileManager instance()
	{
		if (_instance == null)
			_instance = new WindowsXPFileManager();
		return _instance;
	}

	public long created(File file)
	{
		return FileUtils.created(file);
	}

	public long freeSpace(File file)
	{
		return FileUtils.freeSpace(file);
	}

	public long totalSpace(File file)
	{
		return FileUtils.totalSpace(file);
	}

	public boolean chmod(File file, int mode)
	{
		// posix only
		return false;
	}

}
