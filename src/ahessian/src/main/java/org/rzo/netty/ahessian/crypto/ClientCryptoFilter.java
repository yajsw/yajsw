package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

import org.rzo.netty.ahessian.log.OutLogger;

public class ClientCryptoFilter extends ChannelHandlerAdapter implements CryptoConstants
{
	private StreamCipher _encodeCipher;
	private StreamCipher _decodeCipher;
	private byte[] _encodedPublicKey;
	private int _bytesRead;
	private SecureRandom _secureRandom = new SecureRandom();
	private byte[] _password = new byte[PASSWORD_SIZE];
	
	public ClientCryptoFilter()
	{
		super();
		Arrays.fill(_password, (byte)0);
	}


	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object e) throws Exception
            {
				// have we sent our secret key ?
				if (_decodeCipher != null)
				{
					// decode and send upstream
					ByteBuf m = Util.code(_decodeCipher, (ByteBuf) e, true);
					ctx.fireChannelRead(e);
				}
				// we are still in the crypto protocol
				else
				{
					ByteBuf b = (ByteBuf) e;
					// is this our first message ?
					if (_encodedPublicKey == null)
					{
						int size = b.readInt();
						_encodedPublicKey = new byte[size];
					}
					// readin the server's public key
					// it may come in multiple chunks
					int available = b.readableBytes();
					int toRead = Math.min(_encodedPublicKey.length - _bytesRead, available);
					b.readBytes(_encodedPublicKey, _bytesRead, toRead);
					_bytesRead += toRead;
					// we have completed reception of the public key ?
					if (_bytesRead == _encodedPublicKey.length)
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
        KeySpec ks = new X509EncodedKeySpec(_encodedPublicKey);
        Key pubKey = fact.generatePublic(ks);
		String type = "".equals(ASYM_CIPHER_TYPE) ? ASYM_KEY_TYPE : ASYM_KEY_TYPE+"/"+ASYM_CIPHER_TYPE;
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
	//private Key getSymKey()
	{
		 // generate a random secret key
		try
		{
//			KeyGenerator keyGenerator = KeyGenerator.getInstance(SYM_KEY_TYPE);
//			keyGenerator.init(SYM_KEY_SIZE);
//        return keyGenerator.generateKey();
			byte[] key = new byte[SYM_KEY_SIZE];
			_secureRandom.nextBytes(key);
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
		_secureRandom.nextBytes(iv);
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
			if (_password != null)
				b.write(_password);
			b.flush();
			
			System.out.println("generated iv+key: "+OutLogger.asString(b.toByteArray()));
			
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
			_encodeCipher = StreamCipherFactory.createCipher(SYM_KEY_TYPE);
			_encodeCipher.engineInitEncrypt(symKeyEncoded, ivEncoded);
			
			_decodeCipher = StreamCipherFactory.createCipher(SYM_KEY_TYPE);
			_decodeCipher.engineInitDecrypt(symKeyEncoded, ivEncoded);

		// inform others in the pipeline that a secure connection has been established
		ctx.fireChannelActive();
		
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}


	
	@Override
	public void write(ChannelHandlerContext ctx, Object e, ChannelPromise promise) throws Exception
            {
		// if we can encode
		if (_encodeCipher != null)
		{
			// encode the message and send it downstream
			ByteBuf m = Util.code(_encodeCipher, (ByteBuf) e, false);
			ctx.write(m, promise);
		}
		// else ignore. this should not happen, since we have not yet propagated the connected event.
		
            }
	
	public void setPassword(byte[] password)
	{
		if (password == null || password.length == 0)
			return;
		int length = Math.min(PASSWORD_SIZE, password.length);
		System.arraycopy(password, 0, _password, 0, length);
	}


	
	public static void main(String[] args)
	{
		ServerCryptoFilter s = new ServerCryptoFilter();
		ClientCryptoFilter c = new ClientCryptoFilter();
		c._encodedPublicKey = s.getPublicKeyEncoded();
		c.sendSecretKey(null);
	}
    

}
