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

package org.rzo.netty.ahessian.rpc.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.io.InputStreamBuffer;

/**
 * The Class Hessian2Input.
 */
public class Hessian2Input extends com.caucho.hessian4.io.Hessian2Input
{

	boolean _closed = false;
	/**
	 * Instantiates a new hessian2 input.
	 * 
	 * @param is
	 *            the is
	 */
	volatile InputStreamBuffer _isb;

	public Hessian2Input(InputStream is)
	{
		super(is);
		_isb = (InputStreamBuffer) is;
	}

	public boolean bufferEmpty()
	{
		if (_isb == null)
			return true;
		try
		{
			return _isb.available() == 0 && _length <= _offset;
		}
		catch (IOException e)
		{
			Constants.ahessianLogger.warn("", e);
			return true;
		}
	}

	public void close() throws IOException
	{
		_closed = true;
		super.close();
	}

	public boolean isClosed()
	{
		return _closed;
	}

	/**
	 * Read headers.
	 * 
	 * @return the map
	 */
	public Map readHeaders()
	{
		Map result = new HashMap();
		String header = null;
		try
		{
			header = readHeader();
		}
		catch (IOException e)
		{
			Constants.ahessianLogger.warn("", e);
		}
		while (header != null)
		{
			try
			{
				result.put(header, readObject());
				header = readHeader();
			}
			catch (IOException e)
			{
				Constants.ahessianLogger.warn("", e);
				header = null;
			}
		}
		return result;
	}

	public InputStream getInputStream()
	{
		return _isb;
	}

}
