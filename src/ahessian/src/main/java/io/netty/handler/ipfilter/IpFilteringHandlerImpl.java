/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.netty.handler.ipfilter;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

// TODO: Auto-generated Javadoc
/**
 * General class that handle Ip Filtering.
 * 
 * @author frederic bregier
 */
public abstract class IpFilteringHandlerImpl extends ChannelInboundHandlerAdapter implements IpFilteringHandler
{

	private static final AttributeKey<Object> BLOCKED = AttributeKey
			.valueOf("BLOCKED");
	private IpFilterListener listener = null;

	/**
	 * Called when the channel is connected. It returns True if the
	 * corresponding connection is to be allowed. Else it returns False.
	 * 
	 * @param ctx
	 * @param e
	 * @param inetSocketAddress
	 *            the remote {@link InetSocketAddress} from client
	 * @return True if the corresponding connection is allowed, else False.
	 * @throws Exception
	 */
	protected abstract boolean accept(ChannelHandlerContext ctx,
			InetSocketAddress inetSocketAddress) throws Exception;

	/**
	 * Called when the channel has the CONNECTED status and the channel was
	 * refused by a previous call to accept(). This method enables your
	 * implementation to send a message back to the client before closing or
	 * whatever you need. This method returns a ChannelFuture on which the
	 * implementation will wait uninterruptibly before closing the channel.<br>
	 * For instance, If a message is sent back, the corresponding ChannelFuture
	 * has to be returned.
	 * 
	 * @param ctx
	 * @param e
	 * @param inetSocketAddress
	 *            the remote {@link InetSocketAddress} from client
	 * @return the associated ChannelFuture to be waited for before closing the
	 *         channel. Null is allowed.
	 * @throws Exception
	 */
	protected ChannelFuture handleRefusedChannel(ChannelHandlerContext ctx,
			InetSocketAddress inetSocketAddress) throws Exception
	{
		if (listener == null)
			return null;
		ChannelFuture result = listener.refused(ctx, inetSocketAddress);
		return result;
	}

	protected ChannelFuture handleAllowedChannel(ChannelHandlerContext ctx,
			InetSocketAddress inetSocketAddress) throws Exception
	{
		if (listener == null)
			return null;
		ChannelFuture result = listener.allowed(ctx, inetSocketAddress);
		return result;
	}

	/**
	 * Internal method to test if the current channel is blocked. Should not be
	 * overridden.
	 * 
	 * @param ctx
	 * @return True if the current channel is blocked, else False
	 */
	protected boolean isBlocked(ChannelHandlerContext ctx)
	{
		return ctx.attr(BLOCKED).get() != null;
	}

	/**
	 * Called in handleUpstream, if this channel was previously blocked, to
	 * check if whatever the event, it should be passed to the next entry in the
	 * pipeline.<br>
	 * If one wants to not block events, just overridden this method by
	 * returning always true.<br>
	 * <br>
	 * <b>Note that OPENED and BOUND events are still passed to the next entry
	 * in the pipeline since those events come out before the CONNECTED event
	 * and so the possibility to filter the connection.</b>
	 * 
	 * @param ctx
	 * @param e
	 * @return True if the event should continue, False if the event should not
	 *         continue since this channel was blocked by this filter
	 * @throws Exception
	 */
	protected boolean continues(ChannelHandlerContext ctx) throws Exception
	{
		if (listener != null)
			return listener.continues(ctx);
		else
			return false;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception
	{
		if (isBlocked(ctx) && !continues(ctx))
		{
			// don't pass to next level since channel was blocked early
			return;
		}
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
		if (!accept(ctx, inetSocketAddress))
		{
			ctx.attr(BLOCKED).set(true);
			final ChannelFuture future1 = handleRefusedChannel(ctx, inetSocketAddress);
			if (future1 != null)
			{
				future1.addListener(new GenericFutureListener<Future<? super Void>>()
				{

					@Override
					public void operationComplete(Future<? super Void> future)
							throws Exception
					{
						future1.channel().close();
					}
				});
			}
			else
			{
				ctx.channel().close();
			}
			if (isBlocked(ctx) && !continues(ctx))
			{
				// don't pass to next level since channel was
				// blocked early
				return;
			}
		}
		else
		{
			handleAllowedChannel(ctx, inetSocketAddress);
		}
		// This channel is not blocked
		ctx.attr(BLOCKED).remove();
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		if (isBlocked(ctx) && !continues(ctx))
		{
			// don't pass to next level since channel was blocked
			// early
			return;
		}
		ctx.fireChannelInactive();

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.handler.ipfilter.IpFilteringHandler#setIpFilterListener
	 * (org.jboss.netty.handler.ipfilter.IpFilterListener)
	 */
	public void setIpFilterListener(IpFilterListener listener)
	{
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.handler.ipfilter.IpFilteringHandler#removeIpFilterListener
	 * ()
	 */
	public void removeIpFilterListener()
	{
		this.listener = null;

	}
	
	public void channelReadComplete(ChannelHandlerContext ctx)
	{
		ctx.fireChannelReadComplete();
	}

}
