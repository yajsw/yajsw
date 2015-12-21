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

package org.rzo.yajsw.io;

import java.io.IOException;
import java.io.OutputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class TeeOutputStream.
 */
public class TeeOutputStream extends OutputStream
{

	/** The sinks. */
	OutputStream[] sinks = new OutputStream[0];

	/**
	 * Connect.
	 * 
	 * @param sink
	 *            the sink
	 */
	public synchronized void connect(OutputStream sink)
	{
		OutputStream[] newSinks = new OutputStream[sinks.length + 1];
		for (int i = 0; i < sinks.length; i++)
		{
			if (sink != sinks[i])
				newSinks[i] = sinks[i];
			else
				return;
		}
		newSinks[newSinks.length - 1] = sink;
		sinks = newSinks;
	}

	/**
	 * Disconnect.
	 * 
	 * @param sink
	 *            the sink
	 */
	public synchronized void disconnect(OutputStream sink)
	{
		if (sinks.length == 0)
			return;
		OutputStream[] newSinks = new OutputStream[sinks.length - 1];
		int j = 0;
		boolean removed = false;
		for (int i = 0; i < sinks.length && j < newSinks.length; i++)
		{
			if (sink != sinks[i])
			{
				newSinks[j] = sinks[i];
				j++;
			}
			else
				removed = true;
		}
		if (removed)
			sinks = newSinks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public synchronized void write(int b) throws IOException
	{
		for (int i = 0; i < sinks.length; i++)
			sinks[i].write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public synchronized void write(byte b[]) throws IOException
	{
		for (int i = 0; i < sinks.length; i++)
			sinks[i].write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public synchronized void write(byte b[], int off, int len)
			throws IOException
	{
		for (int i = 0; i < sinks.length; i++)
			sinks[i].write(b, off, len);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public synchronized void close() throws IOException
	{
		for (int i = 0; i < sinks.length; i++)
			sinks[i].close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public synchronized void flush() throws IOException
	{
		for (int i = 0; i < sinks.length; i++)
			sinks[i].flush();
	}

}
