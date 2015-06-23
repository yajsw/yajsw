package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

import org.rzo.netty.ahessian.log.OutLogger;

public class ServerCryptoFilterOutbound extends ChannelOutboundHandlerAdapter implements CryptoConstants
{
	ServerCryptoData _data;


	public ServerCryptoFilterOutbound(ServerCryptoData data)
	{
		_data = data;
	}


	@Override
	public void write(ChannelHandlerContext ctx, Object e, ChannelPromise promise) throws Exception
            {
		if (_data._encodeCipher != null)
		{
			ByteBuf m = Util.code(_data._encodeCipher, (ByteBuf) e, false);
			ctx.write(m);
		}
		
            }



}
