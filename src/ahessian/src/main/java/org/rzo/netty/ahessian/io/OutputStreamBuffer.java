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
package org.rzo.netty.ahessian.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.caucho.hessian4.io.FlushableOutput;

/**
 * A buffer for storing outgoing bytes. Bytes are sent upstream if The number of
 * written bytes is > than a watermark, or if flush is called
 */
public class OutputStreamBuffer extends OutputStream implements FlushableOutput
{

	/** The context of the channel on which to send the bytes downstream */
	private volatile ChannelHandlerContext _ctx;

	/** Indicates if the stream has been closed */
	private volatile boolean _closed = false;
	private Lock _lock = new ReentrantLock();

	/** If written bytes > watermark, the bytes are sent downstream */
	int _watermark = 1024 * 1024;
	int _initialBuffSize = 1024;

	/**
	 * The buffer for storing outgoing bytes. Once the bytes have been sent
	 * downstream a new buffer is created
	 */
	private volatile ByteBuf _buf = null;
	ExecutorService _executor = Executors.newSingleThreadExecutor();

	private boolean _immediateFlush = true;

	/**
	 * Instantiates a new output stream buffer.
	 * 
	 * @param ctx
	 *            the context in which bytes are sent downstream
	 */
	OutputStreamBuffer(ChannelHandlerContext ctx)
	{
		super();
		_ctx = ctx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException
	{
		if (_closed)
			throw new IOException("stream closed");
		_lock.lock();
		try
		{
			// System.out.println("write "+_buf.readableBytes());
			checkBuf();
			_buf.writeByte((byte) b);
			if (_buf.writerIndex() >= _watermark)
				sendDownstream(null);
		}
		finally
		{
			_lock.unlock();
		}
	}

	private void checkBuf()
	{
		if (_buf == null)
		{
			_buf = _ctx.alloc().buffer(1024);
			_buf.retain();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException
	{
		if (_closed)
			throw new IOException("stream closed");
		_lock.lock();
		try
		{
			checkBuf();

			_buf.writeBytes(b, off, len);
			// System.out.println("write "+len+" "+_buf.readableBytes());
			if (_buf.writerIndex() >= _watermark)
				sendDownstream(null);
		}
		finally
		{
			_lock.unlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException
	{
		flush(null);
	}

	public void flush(ChannelPromise future) throws IOException
	{
		// System.out.println(System.currentTimeMillis()+" +flush");
		_lock.lock();
		try
		{
			super.flush();
			if (_buf == null || _buf.readableBytes() == 0)
			{
				if (future != null)
					future.setSuccess();
			}
			else
				/*
				 * if (future == null) { sendDownstream(null); _ctx.flush();
				 * //f.await(20000); //if (f != null && !f.await(10000)) //throw
				 * new IOException("write longer than 10 secs"); //
				 * System.out.println("write took longer than 10 2"); } else {
				 * sendDownstream(future); _ctx.flush(); }
				 */
				sendDownstream(future);
			if (_immediateFlush)
				_ctx.flush();
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		finally
		{
			_lock.unlock();
		}
		// System.out.println(System.currentTimeMillis()+" -flush");
	}

	private void sendDownstream(ChannelPromise future) throws IOException
	{
		sendDownstream(future, true);
	}

	private void sendDownstream(final ChannelPromise future, boolean wait)
			throws IOException
	{
		if (future != null && future.isDone())
			return;
		if (!_ctx.channel().isActive())
			throw new IOException("channel disconnected");
		_lock.lock();
		ChannelFuture result = null;
		try
		{
			final ByteBuf toSend = _buf;
			_buf = null;
			if (toSend.refCnt() > 1)
				toSend.release(toSend.refCnt() - 1);
			// System.out.println(System.currentTimeMillis()+" outputstream send downstream +");
			if (future == null || future.isDone())
				result = _ctx.write(toSend);
			else
				_ctx.write(toSend, future);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			_lock.unlock();
		}
		// System.out.println(System.currentTimeMillis()+" outputstream send downstream -");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException
	{
		_lock.lock();
		_closed = true;
		_executor.shutdownNow();
		_lock.unlock();
		// System.out.println("buffer closed");
	}

	public void setContext(ChannelHandlerContext ctx)
	{
		_ctx = ctx;
		reset();
	}

	public ChannelHandlerContext getContext()
	{
		return _ctx;
	}

	public void reset()
	{
		_lock.lock();
		_buf = Unpooled.buffer(1024);
		_closed = false;
		_lock.unlock();
	}

}
