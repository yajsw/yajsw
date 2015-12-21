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
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthTokenList implements AuthToken
{

	private Map<ByteArrayWrapper, AuthToken> _tokens;
	int _receivedLength = 0;
	byte[] _receivedBytes;
	AuthToken _currentToken;
	boolean _uniqueLogon;
	private static final InternalLogger logger = InternalLoggerFactory
			.getInstance(SimpleAuthToken.class);

	public AuthTokenList(Map<ByteArrayWrapper, AuthToken> tokens,
			int bytesLength, boolean uniqueLogon)
	{
		_tokens = tokens;
		_receivedBytes = new byte[bytesLength];
		_uniqueLogon = uniqueLogon;
	}

	public static AuthTokenList fromList(List<AuthToken> tokens,
			boolean uniqueLogon)
	{
		Map<ByteArrayWrapper, AuthToken> tks = new HashMap<ByteArrayWrapper, AuthToken>();
		int bytesLength = 0;
		for (AuthToken token : tokens)
		{
			byte[] pwd = ((SimpleAuthToken) token).getPassword();
			tks.put(new ByteArrayWrapper(pwd), token);
			if (bytesLength < pwd.length)
				bytesLength = pwd.length;
		}
		return new AuthTokenList(tks, bytesLength, uniqueLogon);
	}

	public static AuthTokenList fromList(List<AuthToken> tokens)
	{
		return fromList(tokens, false);
	}

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
		if (_receivedLength == _receivedBytes.length)
		{
			_currentToken = _tokens.get(new ByteArrayWrapper(_receivedBytes));
			if (_currentToken != null
					&& (_uniqueLogon || _currentToken.isLoggedOn()))
			{
				logger.info("authenticated");
				((SimpleAuthToken) _currentToken).setLoggedOn(true);
				if (b.readableBytes() != 0)
					ctx.fireChannelRead(e);
				return PASSED;
			}
			else
			{
				_currentToken = null;
				return FAILED;
			}
		}
		else
			return NOT_COMPLETE;

	}

	public AuthToken authenticate(String password) throws Exception
	{
		ByteArrayWrapper input = new ByteArrayWrapper(
				password.getBytes("UTF-8"));
		AuthToken result = _tokens.get(input);
		if (result == null)
			return null;
		if (_uniqueLogon && result.isLoggedOn())
			return null;
		((SimpleAuthToken) result).setLoggedOn(true);
		return result;
	}

	public void sendPassword(ChannelHandlerContext ctx)
	{
		;
	}

	public boolean isLoggedOn()
	{
		return _currentToken != null;
	}

	public void setLoggedOn(boolean value)
	{
		if (!value && _currentToken != null)
		{
			((SimpleAuthToken) _currentToken).setLoggedOn(false);
			_currentToken = null;
		}
	}

	public void disconnected()
	{
		setLoggedOn(false);
	}

}
