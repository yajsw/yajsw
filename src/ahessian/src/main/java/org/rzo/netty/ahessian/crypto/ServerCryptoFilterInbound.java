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
package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

import org.rzo.netty.ahessian.log.OutLogger;

public class ServerCryptoFilterInbound extends ChannelInboundHandlerAdapter
		implements CryptoConstants
{
	KeyPair _serverKeyPair;
	Key _clientKey;
	private StreamCipher _decodeCipher;
	private byte[] _cryptedIvKeyMessage;
	private int _bytesRead;
	private List<byte[]> _passwords = new ArrayList<byte[]>();
	ServerCryptoData _data;

	public ServerCryptoFilterInbound(ServerCryptoData data)
	{
		_data = data;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		// send public key
		sendByteArray(ctx, getPublicKeyEncoded());
	}

	private void sendByteArray(ChannelHandlerContext ctx, byte[] buffer)
	{
		try
		{
			Channel channel = ctx.channel();
			ByteBuf b = Unpooled.buffer();
			// first send encoded key bytes size
			b.writeInt(buffer.length);
			// then the public key
			b.writeBytes(buffer);
			ctx.write(b);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	byte[] getPublicKeyEncoded()
	{
		try
		{
			// generate a key pair
			SecureRandom random = new SecureRandom();
			KeyPairGenerator generator = KeyPairGenerator
					.getInstance(ASYM_KEY_TYPE);
			generator.initialize(ASYM_KEY_SIZE, random);

			_serverKeyPair = generator.generateKeyPair();
			Key pubKey = _serverKeyPair.getPublic();
			return pubKey.getEncoded();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		// have we sent our secret key ?
		if (_decodeCipher != null)
		{
			ByteBuf m = Util.code(_decodeCipher, (ByteBuf) e, true);
			ctx.fireChannelRead(m);
		}
		else
		{
			ByteBuf b = (ByteBuf) e;
			// is this our first message ?
			if (_cryptedIvKeyMessage == null)
			{
				int size = b.readInt();
				// consistency check, so we do not get an out of memory
				// exception
				if (size > 1024)
				{
					ctx.channel().close();
					return;
				}
				_cryptedIvKeyMessage = new byte[size];
			}
			// readin the client's secret key and iv
			int available = b.readableBytes();
			int toRead = Math.min(_cryptedIvKeyMessage.length - _bytesRead,
					available);
			b.readBytes(_cryptedIvKeyMessage, _bytesRead, toRead);
			_bytesRead += toRead;
			// we have completed receiption ?
			if (_bytesRead == _cryptedIvKeyMessage.length)
			{
				boolean ok = false;
				try
				{
					createCiphers();
					ok = true;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					ctx.channel().close();
				}
				// inform pipline that we are ready for encrypted communication
				if (ok)
					ctx.fireChannelActive();
			}
		}
	}

	private void createCiphers() throws Exception
	{
		// first decode the received data
		String type = "".equals(ASYM_CIPHER_TYPE) ? ASYM_KEY_TYPE
				: ASYM_KEY_TYPE + "/" + ASYM_CIPHER_TYPE;
		Cipher asymCipher = Cipher.getInstance(type);
		asymCipher.init(Cipher.DECRYPT_MODE, _serverKeyPair.getPrivate());
		byte[] data = asymCipher.doFinal(_cryptedIvKeyMessage);

		System.out.println("received iv+key: " + OutLogger.asString(data));

		byte[] iv = new byte[SYM_IV_SIZE];
		System.arraycopy(data, data.length
				- (SYM_IV_SIZE + SYM_KEY_SIZE + PASSWORD_SIZE + PASSWORD_SIZE),
				iv, 0, iv.length);
		System.out.println("received iv: " + OutLogger.asString(iv));

		byte[] key = new byte[SYM_KEY_SIZE];
		System.arraycopy(data, data.length - (SYM_KEY_SIZE + PASSWORD_SIZE),
				key, 0, key.length);
		System.out.println("received key: " + OutLogger.asString(key));

		byte[] password = new byte[PASSWORD_SIZE];
		System.arraycopy(data, data.length - PASSWORD_SIZE, password, 0,
				password.length);
		if (!checkPassword(password))
			throw new RuntimeException("password mismatch");

		_data._encodeCipher = StreamCipherFactory.createCipher(SYM_KEY_TYPE);
		_data._encodeCipher.engineInitEncrypt(key, iv);

		_decodeCipher = StreamCipherFactory.createCipher(SYM_KEY_TYPE);
		_decodeCipher.engineInitDecrypt(key, iv);
	}

	private boolean checkPassword(byte[] password)
	{
		if (password == null || password.length != PASSWORD_SIZE)
			return false;
		else
			for (byte[] pwd : _passwords)
				if (Arrays.equals(password, pwd))
					return true;
		return false;
	}

	public void addPassword(byte[] password)
	{
		if (password == null || password.length == 0 || PASSWORD_SIZE == 0)
			return;
		byte[] mPassword = new byte[PASSWORD_SIZE];
		Arrays.fill(mPassword, (byte) 0);
		int length = Math.min(PASSWORD_SIZE, password.length);
		System.arraycopy(password, 0, mPassword, 0, length);
		_passwords.add(mPassword);
	}

	public static void main(String[] args)
	{
		ServerCryptoFilterInbound h = new ServerCryptoFilterInbound(
				new ServerCryptoData());
		h.getPublicKeyEncoded();
	}

}
