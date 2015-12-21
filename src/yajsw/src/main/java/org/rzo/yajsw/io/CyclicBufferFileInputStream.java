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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import org.rzo.yajsw.util.DaemonThreadFactory;
import org.rzo.yajsw.util.MyReentrantLock;

// TODO: Auto-generated Javadoc
/**
 * The Class CyclicBufferFileInputStream.
 */
public class CyclicBufferFileInputStream extends BufferedInputStream
{

	/**
	 * Instantiates a new cyclic buffer file input stream.
	 * 
	 * @param file
	 *            the file
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public CyclicBufferFileInputStream(File file, String mode)
			throws FileNotFoundException, IOException
	{
		super(newInputStream(file, mode));
	}

	public CyclicBufferFileInputStream(File file) throws FileNotFoundException,
			IOException
	{
		super(newInputStream(file, "rw"));
	}

	/**
	 * New input stream.
	 * 
	 * @param file
	 *            the file
	 * 
	 * @return the input stream
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static InputStream newInputStream(final File file, final String mode)
			throws IOException
	{

		return new InputStream()
		{

			volatile boolean closed = false;
			volatile boolean opened = false;
			Lock lock = new MyReentrantLock();
			RandomAccessFile raf;
			ByteBuffer buf;
			ByteBuffer posBuf;
			ByteBuffer lockBuf;

			synchronized void open() throws IOException
			{
				if (opened)
					return;
				while (!opened && !closed)
				{
					lock.lock();
					if (file.exists())
						try
						{
							// System.out.println("open "+file);
							if (raf == null)
								raf = new RandomAccessFile(file, mode);
							buf = raf.getChannel().map(
									FileChannel.MapMode.READ_ONLY, 5,
									CyclicBufferFilePrintStream.length - 5);
							posBuf = raf.getChannel().map(
									FileChannel.MapMode.READ_ONLY, 1, 4);
							lockBuf = raf.getChannel().map(
									FileChannel.MapMode.READ_ONLY, 0, 1);
							opened = true;
						}
						catch (Exception ex)
						{
							System.out.println(file.getName() + ": "
									+ ex.getMessage());
						}
					lock.unlock();
					if (!opened)
						try
						{
							Thread.sleep(200);
						}
						catch (InterruptedException e)
						{
							// e.printStackTrace();
							// Thread.currentThread().interrupt();
							// may cause endless loop
							System.out
									.println("CyclicBufferFileInputStream - sleep interrupted");
							throw new IOException(e);
						}
				}

			}

			@Override
			public void close()
			{
				// lock.lock();
				closed = true;
				try
				{
					if (raf != null)
						raf.close();
					buf = null;
					System.gc();
					Thread.yield();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// lock.unlock();
			}

			@Override
			public int read() throws IOException
			{
				if (!buf.hasRemaining())
				{
					buf.position(0);
				}
				while (getPosition() == buf.position() && !closed)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						return -1;
					}
				}
				if (closed)
				{
					return -1;
				}
				return buf.get();
			}

			private int getPosition()
			{
				posBuf.position(0);
				waitUnlocked();
				return posBuf.getInt();
			}

			private void waitUnlocked()
			{
				return;
				/*
				 * lockBuf.position(0); boolean free = lockBuf.get() == 0; while
				 * (!free) { try { Thread.sleep(50); } catch
				 * (InterruptedException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); return; } lockBuf.position(0); free =
				 * lockBuf.get() == 0; }
				 */
			}

			@Override
			public int read(byte[] bytes, int off, int len) throws IOException
			{
				// System.out.println("read "+buf.position());
				open();
				lock.lock();
				if (!buf.hasRemaining())
				{
					buf.position(0);
				}
				while (!closed && getPosition() == buf.position())
				{
					try
					{
						lock.unlock();
						Thread.sleep(100);
						lock.lock();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
						lock.unlock();
						return -1;
					}
				}
				if (closed)
				{
					lock.unlock();
					return -1;
				}
				int toRead = getPosition() - buf.position();
				if (toRead < 0)
					toRead = buf.remaining();
				if (toRead > len)
					toRead = len;
				buf.get(bytes, off, toRead);
				lock.unlock();
				return toRead;
			}
		};
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args)
	{
		CyclicBufferFileInputStream reader = null;
		try
		{
			reader = new CyclicBufferFileInputStream(new File("test.dat"));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(reader);
		final BufferedReader br = new BufferedReader(isr);
		Executor executor = Executors
				.newCachedThreadPool(new DaemonThreadFactory("test"));
		executor.execute(new Runnable()
		{

			public void run()
			{
				String line = null;
				String prevLine = null;
				try
				{
					while ((line = br.readLine()) != null)
					{
						if (prevLine != null && prevLine.equals(line))
							System.out.println("repetition found " + prevLine);
						// System.out.println(line);
						prevLine = line;
					}
					System.out.println("terminated ");
					System.out.println(line);
					System.out.println(prevLine);
					System.out.flush();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}

		});
		try
		{
			Thread.sleep(1000000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

}
