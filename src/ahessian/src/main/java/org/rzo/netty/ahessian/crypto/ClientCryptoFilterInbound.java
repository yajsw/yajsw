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

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.rzo.netty.ahessian.log.OutLogger;

public class ClientCryptoFilterInbound extends ChannelInboundHandlerAdapter
		implements CryptoConstants
{

	ClientCryptoData _data;

	public ClientCryptoFilterInbound(ClientCryptoData data)
	{
		_data = data;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		// have we sent our secret key ?
		if (_data._decodeCipher != null)
		{
			// decode and send upstream
			ByteBuf m = Util.code(_data._decodeCipher, (ByteBuf) e, true);
			ctx.fireChannelRead(e);
		}
		// we are still in the crypto protocol
		else
		{
			ByteBuf b = (ByteBuf) e;
			// is this our first message ?
			if (_data._encodedPublicKey == null)
			{
				int size = b.readInt();
				_data._encodedPublicKey = new byte[size];
			}
			// readin the server's public key
			// it may come in multiple chunks
			int available = b.readableBytes();
			int toRead = Math.min(_data._encodedPublicKey.length
					- _data._bytesRead, available);
			b.readBytes(_data._encodedPublicKey, _data._bytesRead, toRead);
			_data._bytesRead += toRead;
			// we have completed reception of the public key ?
			if (_data._bytesRead == _data._encodedPublicKey.length)
			{
				// generate our secret key and send it to the server
				sendSecretKey(ctx);
			}
		}
	}

	private Cipher getAsymCipher()
	{
		try
		{
			// generate Cipher using the server's public key
			KeyFactory fact = KeyFactory.getInstance(ASYM_KEY_TYPE);
			KeySpec ks = new X509EncodedKeySpec(_data._encodedPublicKey);
			Key pubKey = fact.generatePublic(ks);
			String type = "".equals(ASYM_CIPHER_TYPE) ? ASYM_KEY_TYPE
					: ASYM_KEY_TYPE + "/" + ASYM_CIPHER_TYPE;
			Cipher result = Cipher.getInstance(type);
			result.init(Cipher.ENCRYPT_MODE, pubKey);
			return result;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	private byte[] getSymKey()
	// private Key getSymKey()
	{
		// generate a random secret key
		try
		{
			// KeyGenerator keyGenerator =
			// KeyGenerator.getInstance(SYM_KEY_TYPE);
			// keyGenerator.init(SYM_KEY_SIZE);
			// return keyGenerator.generateKey();
			byte[] key = new byte[SYM_KEY_SIZE];
			_data._secureRandom.nextBytes(key);
			return key;

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	private byte[] getIv()
	{
		byte[] iv = new byte[SYM_IV_SIZE];
		_data._secureRandom.nextBytes(iv);
		return iv;
	}

	private void sendSecretKey(ChannelHandlerContext ctx)
	{
		try
		{
			// generate our secret key and iv and write it to a buffer
			byte[] symKeyEncoded = getSymKey();
			byte[] ivEncoded = getIv();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			b.write(ivEncoded);
			b.write(symKeyEncoded);
			if (_data._password != null)
				b.write(_data._password);
			b.flush();

			System.out.println("generated iv+key: "
					+ OutLogger.asString(b.toByteArray()));

			// encode it using the server's public key
			Cipher asymCipher = getAsymCipher();
			byte[] encryptedIvSymKey = asymCipher.doFinal(b.toByteArray());
			ByteBuf cb = Unpooled.buffer();
			cb.writeInt(encryptedIvSymKey.length);
			cb.writeBytes(encryptedIvSymKey);

			// send it to the server
			Channel channel = ctx.channel();
			// wait for the message transmission
			ctx.write(cb).sync();

			// we can now accept in/out messages encrypted with our key
			// first create symmetric ciphers
			_data._encodeCipher = StreamCipherFactory
					.createCipher(SYM_KEY_TYPE);
			_data._encodeCipher.engineInitEncrypt(symKeyEncoded, ivEncoded);

			_data._decodeCipher = StreamCipherFactory
					.createCipher(SYM_KEY_TYPE);
			_data._decodeCipher.engineInitDecrypt(symKeyEncoded, ivEncoded);

			// inform others in the pipeline that a secure connection has been
			// established
			ctx.fireChannelActive();

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public void setPassword(byte[] password)
	{
		if (password == null || password.length == 0)
			return;
		int length = Math.min(PASSWORD_SIZE, password.length);
		System.arraycopy(password, 0, _data._password, 0, length);
	}

	public static void main(String[] args)
	{
		ServerCryptoFilterInbound s = new ServerCryptoFilterInbound(
				new ServerCryptoData());
		ClientCryptoFilterInbound c = new ClientCryptoFilterInbound(
				new ClientCryptoData());
		c._data._encodedPublicKey = s.getPublicKeyEncoded();
		c.sendSecretKey(null);
	}

}
