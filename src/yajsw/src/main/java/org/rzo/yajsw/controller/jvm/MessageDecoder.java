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
package org.rzo.yajsw.controller.jvm;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

import org.rzo.yajsw.controller.Message;

public class MessageDecoder extends ByteToMessageDecoder
{
	@Override
	protected void decode(ChannelHandlerContext paramChannelHandlerContext,
			ByteBuf b, List<Object> out) throws Exception
	{
		if (!b.isReadable())
			return;
		byte code = b.readByte();
		b.writerIndex(b.writerIndex());
		String msg = b.toString(Charset.defaultCharset());
		Message result = new Message(code, msg);
		out.add(result);

	}

}
