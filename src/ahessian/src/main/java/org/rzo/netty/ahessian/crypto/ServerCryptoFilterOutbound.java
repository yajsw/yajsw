package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

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
