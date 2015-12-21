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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ServerCryptoFilterOutbound extends ChannelOutboundHandlerAdapter
		implements CryptoConstants
{
	ServerCryptoData _data;

	public ServerCryptoFilterOutbound(ServerCryptoData data)
	{
		_data = data;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object e,
			ChannelPromise promise) throws Exception
	{
		if (_data._encodeCipher != null)
		{
			ByteBuf m = Util.code(_data._encodeCipher, (ByteBuf) e, false);
			ctx.write(m);
		}

	}

}
