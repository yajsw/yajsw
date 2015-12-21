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
 * 
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
 * 
 * and then, within the handler you can use a {@link java.lang.Object} instead
 * of a {@link ChannelBuffer} as a message:
 * 
 * <pre>
 * void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 * {
 * 	// get the message
 * 	Object msg = e.getMessage();
 * 	// return the current time
 * 	ch.write(new Date());
 * }
 * </pre>
 */
public class HessianEncoder extends ChannelOutboundHandlerAdapter
{

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise future) throws Exception
	{
		OutputStream out = OutputStreamHandler.getOutputStream(ctx);
		HessianOutput hout = new HessianOutput(out);
		hout.writeObject(msg);
		hout.flush(future);
	}

}
