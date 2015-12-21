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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.rzo.yajsw.util.DaemonThreadFactory;
import org.rzo.yajsw.util.MyReentrantLock;

// TODO: Auto-generated Javadoc
/**
 * The Class TeeInputStream.
 */
public class TeeInputStream extends InputStream
{

	/** The sources. */
	Source[] sources = new Source[0];

	/** The lock. */
	ReentrantLock lock = new MyReentrantLock();

	/** The data available. */
	Condition dataAvailable = lock.newCondition();

	/** The Constant executor. */
	static private final Executor executor = Executors
			.newCachedThreadPool(new DaemonThreadFactory("TeeInputStream"));

	/**
	 * Connect.
	 * 
	 * @param source
	 *            the source
	 */
	public synchronized void connect(InputStream source)
	{
		lock.lock();
		Source[] newsources = new Source[sources.length + 1];
		for (int i = 0; i < sources.length; i++)
		{
			if (source != sources[i].getInputStream())
				newsources[i] = sources[i];
			else
			{
				lock.unlock();
				return;
			}
		}
		newsources[newsources.length - 1] = new Source(source, dataAvailable);
		sources = newsources;
		executor.execute(newsources[newsources.length - 1]);
		lock.unlock();
	}

	/**
	 * Disconnect.
	 * 
	 * @param source
	 *            the source
	 */
	public synchronized void disconnect(InputStream source)
	{
		lock.lock();
		if (sources.length == 0)
		{
			lock.unlock();
			return;
		}
		Source[] newsources = new Source[sources.length - 1];
		int j = 0;
		boolean removed = false;
		for (int i = 0; i < sources.length && j < newsources.length; i++)
		{
			if (source != sources[i].getInputStream())
			{
				newsources[j] = sources[i];
				j++;
			}
			else
				removed = true;
		}
		if (removed)
			sources = newsources;
		lock.unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		// System.out.println("do tee read ");
		lock.lock();
		while (true)
		{
			for (int i = 0; i < sources.length; i++)
				if (!sources[i].isStop() && sources[i].getBuffer().size() > 0)
				{
					int result = sources[i].getBuffer().get();
					lock.unlock();
					// System.out.println("tee read "+result);
					return result;
				}
			try
			{
				dataAvailable.await();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		// System.out.println("do tee reads ");

		lock.lock();
		try
		{
			while (true)
			{
				for (int i = 0; i < sources.length; i++)
					if (!sources[i].isStop()
							&& sources[i].getBuffer().size() > 0)
					{
						int result = sources[i].getBuffer().get(b, off, len);
						lock.unlock();
						// System.out.println("tee reads "+result);
						return result;
					}
				try
				{
					// System.out.println("+await");
					dataAvailable.await();
					// System.out.println("-await");
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}

		finally
		{
			// lock.unlock();
		}

	}

	/**
	 * The Class Source.
	 */
	class Source implements Runnable
	{

		/** The in. */
		InputStream in;

		/** The buffer. */
		CircularBuffer buffer = new CircularBuffer(512, true);

		/** The buff. */
		byte[] buff = new byte[512];

		/** The stop. */
		volatile boolean stop = false;

		/** The data available. */
		Condition dataAvailable;

		/**
		 * Instantiates a new source.
		 * 
		 * @param in
		 *            the in
		 * @param dataAvailable
		 *            the data available
		 */
		Source(InputStream in, Condition dataAvailable)
		{
			this.in = in;
			this.dataAvailable = dataAvailable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (!stop)
			{
				int c;
				try
				{
					// System.out.println("read");
					c = in.read();
					// System.out.println(c);
					if (c != -1)
					{
						lock.lock();
						buffer.put((byte) c);
						// System.out.println("put");
						try
						{
							// System.out.println("+signal");
							dataAvailable.signal();
							// System.out.println("-signal");
						}
						catch (Exception ex)
						{
							// ex.printStackTrace();
							System.err
									.println("could not read from InputStream "
											+ ex.getMessage());
						}
						lock.unlock();
					}
					else
						stop = true;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					stop = true;
				}
			}

		}

		/**
		 * Gets the buffer.
		 * 
		 * @return the buffer
		 */
		CircularBuffer getBuffer()
		{
			return buffer;
		}

		/**
		 * Checks if is stop.
		 * 
		 * @return true, if is stop
		 */
		boolean isStop()
		{
			return stop;
		}

		/**
		 * Close.
		 */
		void close()
		{
			try
			{
				in.close();
				buffer.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			stop = true;
		}

		/**
		 * Gets the input stream.
		 * 
		 * @return the input stream
		 */
		InputStream getInputStream()
		{
			return in;
		}
	}

	public static void main(String[] args) throws IOException
	{
		TeeInputStream in = new TeeInputStream();
		InputStream inp = System.in;
		System.setIn(in);
		in.connect(inp);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line;
		while ((line = reader.readLine()) != null)
			System.out.println(">" + line);
	}

}
