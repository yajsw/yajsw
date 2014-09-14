package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class Util implements CryptoConstants
{
	static ByteBuf code(StreamCipher cipher, ByteBuf e, boolean decode) throws Exception
	{
		try
		{
		ByteBuf b =  e;
		byte[] encodedData = cipher.crypt(b.array(), b.readerIndex(), b.readableBytes());
		return Unpooled.wrappedBuffer(encodedData);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		
	}
	


}
