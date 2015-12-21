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
package org.rzo.netty.ahessian.rpc.message;

import io.netty.channel.ChannelHandlerContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.io.InputStreamConsumer;
import org.rzo.netty.ahessian.io.InputStreamHandler;
import org.rzo.netty.ahessian.io.OutputStreamHandler;
import org.rzo.netty.ahessian.rpc.io.Hessian2Input;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler;
import org.rzo.netty.ahessian.session.ServerSessionFilter;

import com.caucho.hessian4.io.AbstractSerializerFactory;
import com.caucho.hessian4.io.SerializerFactory;

/**
 * Decodes a call request from an input stream
 */
public class HessianRPCCallDecoder implements InputStreamConsumer, Constants
{

	/** The _factory. */
	HessianRPCServiceHandler _factory;
	SerializerFactory sFactory = new SerializerFactory();
	volatile Hessian2Input in = null;

	public HessianRPCCallDecoder()
	{
		super();
	}

	public HessianRPCCallDecoder(AbstractSerializerFactory serializerFactory)
	{
		super();
		if (serializerFactory != null)
			sFactory.addFactory(serializerFactory);
	}

	public void consume(ChannelHandlerContext ctx, InputStream inx)
	{
		HessianRPCCallMessage result = null;
		boolean getNextMessage = true;
		if (in == null || in.isClosed() || in.getInputStream() != inx)
		{
			in = new Hessian2Input(inx);
			in.setSerializerFactory(sFactory);
		}
		while (ctx.channel().isActive() && getNextMessage)
		{
			try
			{

				if (in.bufferEmpty())
				{
					// we have nothing to parse
					break;
				}

				int ch;
				if ((ch = in.read()) != 'H')
				{
					if (ch == 0)
						ahessianLogger.info("H expected got Ping");
					else
						ahessianLogger.warn("H expected got " + "0x"
								+ Integer.toHexString(ch & 0xff) + " ("
								+ (char) +ch + ")");
					continue;
				}
				in.read();
				in.read();
				in.readEnvelope();
				String h = in.readString();
				if (!HEADER_STRING.equals(h))
				{
					ahessianLogger.warn("missing header");
					continue;
				}

				Map<Object, Object> headers = new HashMap<Object, Object>();
				String methodName = null;
				List values = new ArrayList();

				int l = in.readInt();
				for (int i = 0; i < l; i++)
				{
					Integer key = in.readInt();
					Object value = in.readObject();
					headers.put(key, value);
				}
				in.readCall();
				methodName = in.readMethod();
				int argsLength = in.readInt();

				for (int i = 0; i < argsLength; i++)
					values.add(in.readObject());

				in.completeCall();
				in.completeEnvelope();

				in.resetReferences();
				result = new HessianRPCCallMessage(methodName,
						values.toArray(), headers,
						(OutputStreamHandler) OutputStreamHandler
								.getOutputEncoder(ctx));
				result.setServer(true);
				result.setHasSessionFilter((Boolean) headers
						.get(HAS_SESSION_FILTER_HEADER_KEY));
				result.setSession(ServerSessionFilter.getSession(ctx));
				result.setHandler(InputStreamHandler.getHandler(ctx));
			}
			catch (Exception ex)
			{
				Constants.ahessianLogger.warn("", ex);
				result = null;
			}
			if (in.bufferEmpty())
			{
				getNextMessage = false;
			}
			if (result != null)
			{
				// System.out.println("got call "+result);
				ctx.fireChannelRead(result);
			}
		}
	}

	public boolean isBufferEmpty()
	{
		return in != null && in.bufferEmpty();
	}

	public void setContext(ChannelHandlerContext ctx)
	{
		if (in == null)
		{
			in = new Hessian2Input(InputStreamHandler.getInputStream(ctx));
			in.setSerializerFactory(sFactory);
		}
	}

}
