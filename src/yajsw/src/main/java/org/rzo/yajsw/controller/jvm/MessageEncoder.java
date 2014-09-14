package org.rzo.yajsw.controller.jvm;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.rzo.yajsw.controller.Message;

public class MessageEncoder extends MessageToByteEncoder<Message>
{

	/** The Constant encoder. */
	static final CharsetEncoder	encoder	= Charset.defaultCharset().newEncoder();
	

	@Override
	protected void encode(ChannelHandlerContext ctx,
			Message m, ByteBuf buffer) throws Exception
	{
		buffer.writeByte(m.getCode());
		buffer.writeBytes(encoder.encode(CharBuffer.wrap(m.getMessage())));
		buffer.writeByte((byte) 0);
	}

}
