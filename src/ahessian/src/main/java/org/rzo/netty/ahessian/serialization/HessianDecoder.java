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
import io.netty.channel.ChannelPipeline;

import java.io.IOException;
import java.io.InputStream;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.io.InputStreamConsumer;
import org.rzo.netty.ahessian.io.PullInputStreamConsumer;

import com.caucho.hessian4.io.HessianInput;

/**
 * Decodes a {@link ChannelBuffer} into a {@link java.lang.Object}. A typical
 * setup for a serialization protocol in a TCP/IP socket would be:
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

public class HessianDecoder extends PullInputStreamConsumer
{

	public HessianDecoder()
	{
		super(new InputStreamConsumer()
		{
			InputStream _in;

			@Override
			public void consume(ChannelHandlerContext ctx, InputStream in)
			{
				_in = in;
				try
				{
					HessianInput hin = new HessianInput(_in);
					while (true)
					{
						Object obj = hin.readObject(null);
						if (obj != null)
							ctx.fireChannelRead(obj);
					}
				}
				catch (Exception ex)
				{
					if (ex.getMessage().startsWith("H expected got 0x0 ("))
						Constants.ahessianLogger.info("Ping received");
					else
						Constants.ahessianLogger.debug("", ex);
				}
			}

			@Override
			public boolean isBufferEmpty()
			{
				if (_in == null)
					return true;
				try
				{
					return _in.available() == 0;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				return true;
			}

			@Override
			public void setContext(ChannelHandlerContext ctx)
			{
				// TODO Auto-generated method stub

			}

		});
		// TODO Auto-generated constructor stub
	}

}
