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
import io.netty.channel.ChannelPromise;

import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.io.OutputProducer;
import org.rzo.netty.ahessian.io.OutputStreamHandler;
import org.rzo.netty.ahessian.rpc.io.Hessian2Output;
import org.rzo.netty.ahessian.session.ClientSessionFilter;

import com.caucho.hessian4.io.AbstractSerializerFactory;
import com.caucho.hessian4.io.SerializerFactory;

/**
 * writes a call request to an output stream
 */
public class HessianRPCCallEncoder extends OutputProducer
{
	SerializerFactory sFactory = new SerializerFactory();
	Hessian2Output hOut = null;
	boolean _hasSessionFilter = false;
	boolean _inverseServer = false;

	public HessianRPCCallEncoder(boolean inverseServer, Executor executor)
	{
		this(inverseServer, null, executor);
	}

	public HessianRPCCallEncoder(AbstractSerializerFactory serializerFactory,
			Executor executor)
	{
		this(false, serializerFactory, executor);
	}

	public HessianRPCCallEncoder(Executor executor)
	{
		this(false, null, executor);
	}

	public HessianRPCCallEncoder(boolean inverseServer,
			AbstractSerializerFactory serializerFactory, Executor executor)
	{
		super(executor);
		if (serializerFactory != null)
			sFactory.addFactory(serializerFactory);
		_inverseServer = inverseServer;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelDownstreamHandler#writeRequested
	 * (org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void produceOutput(ChannelHandlerContext ctx, Object e,
			ChannelPromise promise) throws Exception
	{
		if (!ctx.channel().isActive())
			throw new RuntimeException("channel not active");
		try
		{
			/*
			 * Object msg = e; if (msg instanceof FlushRequestMessage) {
			 * ChannelPromise future = ctx.newPromise(); hOut.flush(future);
			 * //future.sync(); return; } no longer required
			 */
			HessianRPCCallMessage message = (HessianRPCCallMessage) e;
			message.setHasSessionFilter(_hasSessionFilter);
			hOut.resetReferences();
			hOut.call(message);
			if (_inverseServer)
				hOut.flush(promise);
		}
		catch (Exception ex)
		{
			Constants.ahessianLogger.warn("", ex);
			promise.setFailure(ex);
		}

	}

	private OutputStream getOutputStream(ChannelHandlerContext ctx)
	{
		return (OutputStream) ctx.channel().attr(OutputStreamHandler.OUTSTREAM)
				.get();
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception
	{
		super.connect(ctx, remoteAddress, localAddress, promise);
		_hasSessionFilter = ctx.pipeline().context(ClientSessionFilter.class) != null;
		if (hOut == null)
		{
			OutputStream out = getOutputStream(ctx);
			hOut = new Hessian2Output(out);
			hOut.getSerializerFactory().addFactory(sFactory);
		}
		else
			hOut.reset();
	}

	@Override
	protected void flashOutput(ChannelHandlerContext ctx) throws Exception
	{
		if (!ctx.channel().isActive())
			throw new RuntimeException("channel not active");
		hOut.flush();
	}

}