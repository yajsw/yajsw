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
package org.rzo.netty.ahessian.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Arrays;

/**
 * A Simple Authentication Token. The password is sent unencrypted.
 */
public class SimpleAuthToken implements AuthToken
{

	/** The _password. */
	byte[] _password;

	/** The _received bytes. */
	byte[] _receivedBytes;

	/** The _received length. */
	int _receivedLength = 0;

	boolean _loggedOn = false;

	int _length = -1;

	private static final InternalLogger logger = InternalLoggerFactory
			.getInstance(SimpleAuthToken.class);

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the new password
	 */
	public void setPassword(String password)
	{
		_password = ensureLength(password.getBytes());
		_receivedBytes = new byte[_password.length];
	}

	public void setLength(int length)
	{
		_length = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rzo.netty.ahessian.auth.AuthToken#authenticate(org.jboss.netty.channel
	 * .ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	public int authenticate(ChannelHandlerContext ctx, ByteBuf e)
	{
		ByteBuf b = e;
		int toCopy = Math.min(_receivedBytes.length - _receivedLength,
				b.readableBytes());
		byte[] bytes = new byte[toCopy];
		b.readBytes(bytes);
		System.arraycopy(bytes, 0, _receivedBytes, _receivedLength,
				bytes.length);
		_receivedLength += toCopy;
		if (_receivedLength == _password.length)
		{
			if (Arrays.equals(_receivedBytes, _password))
			{
				logger.info("authenticated");
				if (b.readableBytes() != 0)
					ctx.fireChannelRead(e);
				return PASSED;
			}
			else
				return FAILED;
		}
		else
			return NOT_COMPLETE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rzo.netty.ahessian.auth.AuthToken#sendPassword(org.jboss.netty.channel
	 * .ChannelHandlerContext)
	 */
	public void sendPassword(ChannelHandlerContext ctx)
	{
		ctx.write(Unpooled.wrappedBuffer(_password));
	}

	public boolean isLoggedOn()
	{
		return _loggedOn;
	}

	void setLoggedOn(boolean loggedOn)
	{
		_loggedOn = loggedOn;
	}

	public void disconnected()
	{
		setLoggedOn(false);
	}

	byte[] ensureLength(byte[] bytes)
	{
		if (bytes.length == _length || _length <= 0)
			return bytes;
		else
		{
			return Arrays.copyOf(bytes, _length);
		}
	}

	byte[] getPassword()
	{
		return _password;
	}

}
