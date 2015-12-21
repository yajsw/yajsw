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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.io.InputStream;

import org.rzo.netty.ahessian.stopable.StopableHandler;

//import com.caucho.services.server.ServiceContext;

/**
 * Encodes bytes read by a {@link Channel} into an {@link InputStream}. <br>
 * Once created the InputStream is passed as a message to the next handler
 * within a separate thread. From there on, no further messages are passed
 * through the pipeline. <br>
 * A typical setup for a serialization protocol in a TCP/IP socket would be:
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
public class InputStreamHandler extends ChannelInboundHandlerAdapter implements
		StopableHandler
{

	/** Thread pool for getting a thread for calling the next handler */
	volatile InputStreamBuffer _in = null;
	boolean _stopEnabled = true;
	boolean _crcCheck = false;
	private volatile Runnable _disconnectListener = null;
	private static AttributeKey<InputStreamBuffer> INSTREAM = AttributeKey
			.valueOf("INSTREAM");

	public InputStreamHandler()
	{
	}

	public InputStreamHandler(boolean crcCheck)
	{
		_crcCheck = crcCheck;
	}

	/**
	 * Instantiates a new input stream decoder.
	 * 
	 * @param executor
	 *            the thread pool
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(
	 * org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object e)
			throws Exception
	{
		// System.out.println(System.currentTimeMillis()+" InputStreamHanlder.messageReceived +");
		_in.write(((ByteBuf) e));
		ctx.fireChannelReadComplete();
		ctx.fireChannelRead(_in);
		// System.out.println(System.currentTimeMillis()+" InputStreamHanlder.messageReceived -");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		if (_in == null)
		{
			// System.out.println("new input stream buffer");
			if (_crcCheck)
				_in = new CRCInputStream();
			else
				_in = new InputStreamBuffer();
			ctx.channel().attr(INSTREAM).set(_in);
		}
		// ctx.fireChannelActive();
		super.channelActive(ctx);
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		if (_disconnectListener != null)
			_disconnectListener.run();
		ctx.fireChannelInactive();
	}

	public static InputStreamBuffer getInputStream(ChannelHandlerContext ctx)
	{
		return (InputStreamBuffer) ctx.channel().attr(INSTREAM).get();
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
			_in.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_in = null;
	}

	public void setDisconnectListener(Runnable disconnectListener)
	{
		_disconnectListener = disconnectListener;
	}

	public static InputStreamHandler getHandler(ChannelHandlerContext ctx)
	{
		return ctx.pipeline().get(InputStreamHandler.class);
	}

}
