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
package org.rzo.netty.ahessian.log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.logging.LoggingHandler;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class OutLogger extends LoggingHandler
{
	String _name;
	boolean _stateOnly;
	private static int MSG_LOG_LENGTH = 100;

	public OutLogger(String name)
	{
		this(name, false);
	}

	public OutLogger(String name, boolean stateOnly)
	{
		super(name);
		_name = name;
		_stateOnly = stateOnly;
		Logger logger = Logger.getLogger(name);
		if (!isNew(logger))
			return;
		Logger.getLogger(name).setLevel(Level.ALL);
		ConsoleHandler console = new ConsoleHandler();
		console.setLevel(Level.ALL);
		console.setFormatter(new SimpleFormatter()
		{
			public synchronized String format(LogRecord record)
			{
				return System.currentTimeMillis() + " (" + _name + ") "
						+ record.getMessage() + "\r\n";
			}
		});
		Logger.getLogger(name).addHandler(console);
	}

	private boolean isNew(Logger logger)
	{
		return logger.getHandlers().length == 0;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		if (_stateOnly)
		{
			ctx.fireChannelRead(e);
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append("" + System.currentTimeMillis());
		sb.append("/");
		sb.append(_name);
		sb.append("/");
		sb.append(Thread.currentThread().getName());
		sb.append(" ");
		sb.append(ctx.channel().hashCode());
		sb.append(" <in< ");
		sb.append(']');
		if (e instanceof ByteBuf)
		{
			encodeBuffer((ByteBuf) e, sb);
		}

		if (logger.isEnabled(internalLevel))
		{
			logger.log(internalLevel, sb.toString());
		}

		ctx.fireChannelRead(e);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object e,
			ChannelPromise promise) throws Exception
	{
		if (_stateOnly)
		{
			ctx.write(e, promise);
			return;
		}

		StringBuilder sb = new StringBuilder();

		sb.append('[');
		sb.append("" + System.currentTimeMillis());
		sb.append("/");
		sb.append(_name);
		sb.append("/");
		sb.append(Thread.currentThread().getName());
		sb.append(" ");
		sb.append(ctx.channel().hashCode());
		sb.append(" >out> ");
		sb.append(']');
		if (e instanceof ByteBuf)
			encodeBuffer((ByteBuf) e, sb);
		else
			sb.append(e.toString());

		if (logger.isEnabled(internalLevel))
		{
			logger.log(internalLevel, sb.toString());
		}

		// System.out.println(promise);
		ctx.write(e, promise);
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception
	{
		if (_stateOnly)
		{
			ctx.flush();
			return;
		}
		super.flush(ctx);

	}

	static private void encodeBuffer(ByteBuf buffer, StringBuilder sb)
	{
		if (buffer == null)
			return;
		sb.append("(" + buffer.readableBytes() + ") ");
		int size = Math.min(MSG_LOG_LENGTH, buffer.readableBytes());
		byte[] b = new byte[size];
		buffer.getBytes(0, b);
		for (int i = 0; i < b.length && i < MSG_LOG_LENGTH; i++)
		{
			toDebugChar(sb, b[i]);
		}
	}

	static private void toDebugChar(StringBuilder sb, int ch)
	{
		if (ch >= 0x20 && ch < 0x7f)
		{
			sb.append((char) ch);
		}
		else
			sb.append(String.format("\\x%02x", ch & 0xff));
	}

	public static String asString(byte[] buffer)
	{
		StringBuilder sb = new StringBuilder();
		if (buffer == null)
			return "null";
		sb.append("(" + buffer.length + ") ");
		for (int i = 0; i < buffer.length; i++)
		{
			toDebugChar(sb, buffer[i]);
		}
		return sb.toString();

	}

}
