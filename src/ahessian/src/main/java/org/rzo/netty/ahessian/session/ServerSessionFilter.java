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
package org.rzo.netty.ahessian.session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory.HandlerList;

/**
 * Handles sessions on the server side. A typical setup for a protocol in a
 * TCP/IP socket would be:
 * 
 * <pre>
 * // _mixinFactory is a ChannelPipelineFactory which returns MixinPipeline
 * {@link ChannelPipeline} pipeline = ...;
 * 
 * pipeline.addLast(&quot;sessionFilter&quot;, new ServerSessionFilter(_mixinFactory));
 * </pre>
 */
@Sharable
public class ServerSessionFilter extends ChannelInboundHandlerAdapter
{

	/** Indicates if session has been assigned to the current channel */
	private boolean _hasSession = false;

	/** String for reading in a session id */
	private String _sessionId = "";

	/** Factory for creating new session objects */
	private SessionFactory _factory = new SessionFactory();

	/** A pipeline factory which returns a MixinPipeline */
	private ChannelPipelineFactory _mixinFactory;

	/** Assignment of session-id to the associated MixinPipeline */
	private static Map<String, HandlerList> _sessionPipelines = Collections
			.synchronizedMap(new HashMap<String, HandlerList>());

	private long _sessionTimeout = -1;

	private Timer _timer = null;

	private volatile Channel _channel = null;

	private volatile boolean _valid = true;

	public static final AttributeKey<Session> SESSION = AttributeKey
			.valueOf("SESSION");

	/**
	 * Instantiates a new server session filter.
	 * 
	 * @param mixinFactory
	 *            a pipeline factory which returns MixinPipeline
	 */
	public ServerSessionFilter(ChannelPipelineFactory mixinFactory,
			Timer timer, long sessionTimeout)
	{
		_mixinFactory = mixinFactory;
		_timer = timer;
		_sessionTimeout = sessionTimeout;
	}

	public ServerSessionFilter(ChannelPipelineFactory mixinFactory)
	{
		this(mixinFactory, null, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(
	 * org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	public void channelRead(ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		// if session established forward all messages
		if (_hasSession)
		{
			Session session = ctx.channel().attr(SESSION).get();
			session.onMessage();
			ctx.fireChannelRead(e);
		}
		else
		{
			ByteBuf b = (ByteBuf) e;
			_sessionId += b.toString(Charset.forName("UTF-8"));
			b.release(b.refCnt());
			if (_sessionId.equals("?"))
				newSession(ctx);
			else
				checkSession(ctx);
		}
	}

	private void checkSession(ChannelHandlerContext ctx)
	{
		if (_sessionId.length() == _factory.getSessionIdLength() * 2)
		{
			Session session = _factory.getSession(_sessionId);
			if (session == null)
				newSession(ctx);
			else
				confirmSession(ctx);
		}

	}

	private void newSession(ChannelHandlerContext ctx)
	{
		Session session = _factory.createSession(null);
		Constants.ahessianLogger.info(ctx.channel() + " new session #"
				+ session.getId());
		HandlerList pipeline = null;
		try
		{
			pipeline = (HandlerList) _mixinFactory.getPipeline();
			_sessionPipelines.put(session.getId(), pipeline);
		}
		catch (Exception e)
		{
			Constants.ahessianLogger.warn("", e);
		}
		handleSession(ctx, session, pipeline);
	}

	private void confirmSession(ChannelHandlerContext ctx)
	{
		Session session = _factory.getSession(_sessionId);
		Constants.ahessianLogger.info(ctx.channel() + " reuse session #"
				+ session.getId());
		HandlerList pipeline = _sessionPipelines.get(_sessionId);
		handleSession(ctx, session, pipeline);
	}

	private void handleSession(ChannelHandlerContext ctx, Session session,
			HandlerList pipeline)
	{
		_hasSession = true;
		session.setClosed(false);

		// if we have a session timeout set, cancel it.
		Timeout timeOut = session.removeTimeout();
		if (timeOut != null)
			timeOut.cancel();

		if (pipeline.hasChannel())
		{
			Constants.ahessianLogger.warn(ctx.channel()
					+ " session already attached -> close connection");
			pipeline.close();
		}

		// now that we have a session extend the pipeline
		pipeline.mixin(ctx);
		ctx.channel().attr(SESSION).set(session);
		_channel = ctx.channel();
		// first send session and wait until it has been transmitted
		ctx.writeAndFlush(Unpooled.wrappedBuffer(session.getId().getBytes()))
				.awaitUninterruptibly();
		// only then inform the mixin pipeline that we are connected
		ctx.fireChannelActive();
	}

	/**
	 * Helper Method: returns the session of associated with the pipeline of a
	 * given context
	 * 
	 * @param ctx
	 *            the context
	 * 
	 * @return the session
	 */
	public static Session getSession(ChannelHandlerContext ctx)
	{
		return (Session) ctx.channel().attr(SESSION).get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected
	 * (org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception
	{

		_hasSession = false;
		ctx.channel().attr(SESSION).get().close();
		final String sessionId = ctx.channel().attr(SESSION).get().getId();
		Constants.ahessianLogger.info("Session disconnected: " + sessionId);
		_sessionId = "";
		_channel = null;
		// remove the session if the client does not reconnect within timeout
		if (_sessionTimeout > 0)
		{
			Timeout timeOut = _timer.newTimeout(new TimerTask()
			{

				public void run(Timeout arg0) throws Exception
				{
					ctx.channel().attr(SESSION).get().invalidate();
					_factory.removeSession(sessionId);
					_sessionPipelines.remove(sessionId);
					_valid = false;
					Constants.ahessianLogger.warn(ctx.channel()
							+ " session timed out: " + sessionId);
				}

			}, _sessionTimeout, TimeUnit.MILLISECONDS);
			ctx.channel().attr(SESSION).get().setTimeOut(timeOut);
		}
		ctx.fireChannelInactive();
	}

	public long getSessionTimeout()
	{
		return _sessionTimeout;
	}

	public void setSessionTimeout(long sessionTimeout)
	{
		_sessionTimeout = sessionTimeout;
	}

	public boolean isValid()
	{
		return _valid;
	}

	public Channel getChannel()
	{
		return _channel;
	}

	public static ServerSessionFilter getServerSessionFilter(
			ChannelHandlerContext ctx)
	{
		return (ServerSessionFilter) ctx.pipeline()
				.context(ServerSessionFilter.class).handler();
	}

}
