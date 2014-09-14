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
