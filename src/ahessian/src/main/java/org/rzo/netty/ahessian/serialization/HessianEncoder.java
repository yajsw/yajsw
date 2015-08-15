package org.rzo.netty.ahessian.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

import java.io.OutputStream;

import org.rzo.netty.ahessian.io.OutputStreamHandler;

import com.caucho.hessian4.io.HessianOutput;

/**
 * Encodes the requested {@link java.lang.Object} into a {@link ChannelBuffer}.
 * A typical setup for a serialization protocol in a TCP/IP socket would be:
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 * 
 * // Encoder
 * pipeline.addLast("outputStream", new {@link io.OutputStream}());
 * pipeline.addLast("hessianEncoder", new {@link HessianEncoder}());
 * 
 * // Decoder
 * pipeline.addLast("inputStream", new {@link io.InputStream}());
 * pipeline.addLast("hessianDecoder", new {@link HessianDecoder}());
 * pipeline.addLast("handler", new MyHandler());
 * </pre>
 * and then, within the handler you can use a {@link java.lang.Object} instead of a {@link ChannelBuffer}
 * as a message:
 * <pre>
 * void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
 * // get the message
 * Object msg = e.getMessage();
 * // return the current time
 * ch.write(new Date());
 * }
 * </pre>
 */
public class HessianEncoder extends ChannelOutboundHandlerAdapter
{
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise future) throws Exception
	{
	   	OutputStream out = OutputStreamHandler.getOutputStream(ctx);
		HessianOutput hout = new HessianOutput(out);
		hout.writeObject(msg);
		hout.flush(future);
	}

}
