package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
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

public class ClientCryptoFilterOutbound extends ChannelOutboundHandlerAdapter implements CryptoConstants
{

	ClientCryptoData _data;
	
	public ClientCryptoFilterOutbound(ClientCryptoData data)
	{
		_data = data;
	}


	@Override
	public void write(ChannelHandlerContext ctx, Object e, ChannelPromise promise) throws Exception
            {
		// if we can encode
		if (_data._encodeCipher != null)
		{
			// encode the message and send it downstream
			ByteBuf m = Util.code(_data._encodeCipher, (ByteBuf) e, false);
			ctx.write(m, promise);
		}
		// else ignore. this should not happen, since we have not yet propagated the connected event.
		
            }
    

}
