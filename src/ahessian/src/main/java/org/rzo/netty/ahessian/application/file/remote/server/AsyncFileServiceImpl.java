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
package org.rzo.netty.ahessian.application.file.remote.server;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.rzo.netty.ahessian.application.file.remote.service.AsyncFileService;
import org.rzo.netty.ahessian.application.file.remote.service.FileObject;

public class AsyncFileServiceImpl implements AsyncFileService
{

	public FileObject getFile(String file)
	{
		File f = new File(file);
		if (!f.exists())
			return null;
		return toFileObject(f);
	}

	private FileObject toFileObject(File f)
	{
		return new FileObjectImpl(f);
	}

	public InputStream getInputStream(String file)
	{
		return null;
	}

	public List<FileObject> list(String filter)
	{
		return null;
	}

}
