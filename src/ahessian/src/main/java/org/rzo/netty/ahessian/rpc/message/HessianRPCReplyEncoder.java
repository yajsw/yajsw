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

import com.caucho.hessian4.io.AbstractSerializerFactory;

/**
 * writes an invocation reply message to an output stream
 */
public class HessianRPCReplyEncoder extends OutputProducer
{
	volatile Hessian2Output hOut = null;
	volatile AbstractSerializerFactory _serializerFactory;

	public HessianRPCReplyEncoder(Executor executor)
	{
		this(null, executor);
	}

	public HessianRPCReplyEncoder(AbstractSerializerFactory serializerFactory,
			Executor executor)
	{
		super(executor);
		_serializerFactory = serializerFactory;
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
	synchronized public void produceOutput(ChannelHandlerContext ctx,
			Object msg, ChannelPromise promise) throws Exception
	{
		if (ctx == null || ctx.channel() == null || !ctx.channel().isActive())
			throw new RuntimeException("channel closed");
		/*
		 * if (msg instanceof FlushRequestMessage) { hOut.flush(promise);
		 * promise.await(5000); return; }
		 */

		try
		{
			// if (e.getMessage() instanceof Integer)
			// {
			// hOut.flush();
			// return;
			// }
			HessianRPCReplyMessage message = (HessianRPCReplyMessage) msg;
			// Constants.ahessianLogger.warn("encode reply for #"+message.getHeaders().get(Constants.CALL_ID_STRING));

			hOut.resetReferences();
			hOut.writeReply(message);
			// hOut.flush();
			// e.getFuture().setSuccess();
		}
		catch (Exception ex)
		{
			Constants.ahessianLogger.warn("", ex);
			promise.setFailure(ex);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		if (hOut == null)
		{
			OutputStream out = OutputStreamHandler.getOutputStream(ctx);
			hOut = new Hessian2Output(out);
			if (_serializerFactory != null)
				hOut.getSerializerFactory().addFactory(_serializerFactory);
		}
		else
			hOut.reset();
		super.channelActive(ctx);
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception
	{
		super.connect(ctx, remoteAddress, localAddress, promise);
		if (hOut == null)
		{
			OutputStream out = OutputStreamHandler.getOutputStream(ctx);
			hOut = new Hessian2Output(out);
			if (_serializerFactory != null)
				hOut.getSerializerFactory().addFactory(_serializerFactory);
		}
		else
			hOut.reset();
		ctx.fireChannelActive();
	}

	@Override
	protected void flashOutput(ChannelHandlerContext ctx) throws Exception
	{
		if (!ctx.channel().isActive())
			throw new RuntimeException("channel not active");
		hOut.flush();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception
	{
		ctx.fireChannelRead(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
	{
		ctx.fireChannelReadComplete();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception
	{
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception
	{
		ctx.fireChannelWritabilityChanged();
	}

}
