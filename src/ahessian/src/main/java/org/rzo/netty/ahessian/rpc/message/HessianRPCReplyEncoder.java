package org.rzo.netty.ahessian.rpc.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.OutputStream;
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

	public HessianRPCReplyEncoder(AbstractSerializerFactory serializerFactory, Executor executor)
	{
		super(executor);
		_serializerFactory = serializerFactory;
	}

		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.SimpleChannelDownstreamHandler#writeRequested(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
		 */
	@Override
		synchronized public void produceOutput(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
		{
		if (ctx == null || ctx.channel() == null  || !ctx.channel().isActive())
			throw new RuntimeException("channel closed");
		/*
 			if (msg instanceof FlushRequestMessage)
 			{
 				hOut.flush(promise);
 				promise.await(5000);
 				return;
 			}
 			*/

			try
			{
//				if (e.getMessage() instanceof Integer)
//				{
//					hOut.flush();
//					return;
//				}
			HessianRPCReplyMessage message = (HessianRPCReplyMessage) msg;
			//Constants.ahessianLogger.warn("encode reply for #"+message.getHeaders().get(Constants.CALL_ID_STRING));

			hOut.resetReferences();
			hOut.writeReply(message);
			//hOut.flush();
			//e.getFuture().setSuccess();
			}
			catch (Exception ex)
			{
				Constants.ahessianLogger.warn("", ex);
				promise.setFailure(ex);
			}
		}
		
		public void channelActive(ChannelHandlerContext ctx) throws Exception
		{
			super.channelActive(ctx);
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



}
