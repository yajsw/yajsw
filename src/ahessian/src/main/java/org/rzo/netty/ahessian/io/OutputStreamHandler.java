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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketAddress;

import org.rzo.netty.ahessian.stopable.StopableHandler;

/**
 * Encodes bytes written to an {@link OutputStream} into a {@link ChannelBuffer}
 * . A typical setup for a serialization protocol in a TCP/IP socket would be:
 * 
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 * 
 * // Encoder
 * pipeline.addLast(&quot;outputStream&quot;, new {@link handler.io.OutputStream}());
 * pipeline.addLast(&quot;outputHandler&quot;, new MyOutputHandler());
 * 
 * // Decoder
 * pipeline.addLast(&quot;inputStream&quot;, new {@link handler.io.InputStream}());
 * pipeline.addLast(&quot;inputHandler&quot;, new MyInputHandler());
 * </pre>
 * 
 * and then, within the handler you can use a {@link java.io.InputStream} or
 * {@link java.io.OutputStream} instead of a {@link ChannelBuffer} as a message: <br>
 * Writing to OutputStream:
 * 
 * <pre>
 * // synchronized for multithreaded environment to avoid messages mixing
 * synchronized public void writeRequested(ChannelHandlerContext ctx,
 * 		MessageEvent e) throws Exception
 * {
 * 	byte[] message = (byte[]) e.getMessage();
 * 	OutputStream out = OutputStreamEncoder.getOutputStream(ctx);
 * 	out.write(message);
 * 	// if this is the last chunk of bytes we should flush the output
 * 	out.flush();
 * 	// netty seems to require this, so that the boss thread may read input from
 * 	// the channel
 * 	Thread.yield();
 * }
 * 
 * </pre>
 * 
 * <br>
 * Reading from InputStream:
 * 
 * <pre>
 * void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 * {
 * 	// message received is called only once to deliver the input stream
 * 	// it is called in a separate thread and not in the netty worker thread.
 * 	// incoming bytes are consumed in this method.
 * 	// the stream is closed once the channel is disconnected
 * 	InputStream in = (InputStream) evt.getMessage();
 * 
 * 	while (ctx.getChannel().isConnected())
 * 	{
 * 		// parse the incoming stream and forward the result to the next handler
 * 		Channels.fireMessageReceived(ctx, parseReply(in));
 * 	}
 * }
 * </pre>
 */
public class OutputStreamHandler extends ChannelInboundHandlerAdapter implements
		StopableHandler, ChannelOutboundHandler
{
	volatile OutputStreamBuffer _buffer = null;
	private boolean _stopEnabled = true;
	boolean _crcCheck = false;
	public static AttributeKey<OutputStreamBuffer> OUTSTREAM = AttributeKey
			.valueOf("OUTSTREAM");
	private static AttributeKey<OutputStreamHandler> OUTENCODER = AttributeKey
			.valueOf("OUTENCODER");

	public OutputStreamHandler()
	{

	}

	public OutputStreamHandler(boolean crcCheck)
	{
		_crcCheck = crcCheck;
	}

	/**
	 * Helper method: Gets the output stream from the pipeline of a given
	 * context.
	 * 
	 * @param ctx
	 *            the context
	 * 
	 * @return the output stream
	 */
	public static OutputStream getOutputStream(ChannelHandlerContext ctx)
	{
		return (OutputStream) ctx.channel().attr(OUTSTREAM).get();
	}

	public static OutputStreamHandler getOutputEncoder(ChannelHandlerContext ctx)
	{
		return (OutputStreamHandler) ctx.channel().attr(OUTENCODER).get();
	}

	public OutputStreamBuffer getBuffer()
	{
		return _buffer;
	}

	public boolean isStopEnabled()
	{
		return _stopEnabled;
	}

	public void setStopEnabled(boolean stopEnabled)
	{
		_stopEnabled = stopEnabled;
	}

	public void stop()
	{
		try
		{
			_buffer.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_buffer = null;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		doChannelActive(ctx);
		ctx.fireChannelActive();
	}

	private void doChannelActive(ChannelHandlerContext ctx)
	{
		if (_buffer == null)
		{
			if (_crcCheck)
				_buffer = new CRCOutputStream(ctx);
			else
				_buffer = new OutputStreamBuffer(ctx);
			ctx.channel().attr(OUTSTREAM).set(_buffer);
		}
		else
			_buffer.setContext(ctx);
		ctx.channel().attr(OUTENCODER).set(this);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		doChannelInactive();
		ctx.fireChannelInactive();
	}

	private void doChannelInactive() throws IOException
	{
		if (_buffer != null)
		{
			_buffer.close();
		}
	}

	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress,
			ChannelPromise promise) throws Exception
	{
		ctx.bind(localAddress, promise);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception
	{
		doChannelActive(ctx);
		ctx.connect(remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception
	{
		doChannelInactive();
		ctx.disconnect(promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception
	{
		ctx.close(promise);
	}

	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception
	{
		ctx.deregister(promise);
	}

	@Override
	public void read(ChannelHandlerContext ctx) throws Exception
	{
		ctx.read();
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception
	{
		ctx.write(msg, promise);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception
	{
		ctx.flush();
	}

}
