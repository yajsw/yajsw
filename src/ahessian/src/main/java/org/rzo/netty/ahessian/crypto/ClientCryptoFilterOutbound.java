package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

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
