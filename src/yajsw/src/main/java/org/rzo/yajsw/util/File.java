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
package org.rzo.yajsw.util;

import java.util.Date;

import org.rzo.yajsw.os.OperatingSystem;

public class File extends java.io.File
{

	public File(String pathname)
	{
		super(pathname);
	}

	public long created()
	{
		return OperatingSystem.instance().fileManagerInstance().created(this);
	}

	public long getFreeSpace()
	{
		return OperatingSystem.instance().fileManagerInstance().freeSpace(this);
	}

	public long getTotalSpace()
	{
		return OperatingSystem.instance().fileManagerInstance()
				.totalSpace(this);
	}

	static public void main(String[] args)
	{
		File f = new File("E:");
		System.out.println(new Date(f.created()));
		System.out.println(f.getFreeSpace());
		System.out.println(f.getTotalSpace());
	}

}
