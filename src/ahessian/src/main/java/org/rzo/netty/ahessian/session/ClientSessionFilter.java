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
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory.HandlerList;

/**
 * Handles sessions on the client side. A typical setup for a protocol in a
 * TCP/IP socket would be:
 * 
 * <pre>
 * // client session filter is an attribute of the ChannelPipelineFactory Class
 * // it should not be created with each call to getPipeline()
 * // _mixinFactory is a ChannelPipelineFactory which returns MixinPipeline
 * _sessionFilter = new ClientSessionFilter(_mixinFactory);
 * 
 * {@link ChannelPipeline} pipeline = ...;
 * 
 * pipeline.addLast("sessionFilter", _sessionFilter);
 * </pre>
 */
@Sharable
public class ClientSessionFilter extends ChannelInboundHandlerAdapter
{

	/** The current session. */
	private Session _session = null;

	/** Indicates if we have received a session. */
	private boolean _hasSession = false;

	/** String to read in the session id from the server */
	private String _sessionId = "";

	/** Factory for creating session objects. */
	private SessionFactory _factory = new SessionFactory();

	/** The factory for getting a MixinPipeline for a new session */
	private ChannelPipelineFactory _mixinFactory;

	/**
	 * Assignment of session-id to pipelines created. //TODO destroy a pipeline
	 * if a session is timed out
	 */
	private static Map<String, HandlerList> _sessionPipelines = Collections
			.synchronizedMap(new HashMap<String, HandlerList>());

	private List<Runnable> _sessionClosedListeners = Collections
			.synchronizedList(new ArrayList());

	private List<Runnable> _sessionNewListeners = Collections
			.synchronizedList(new ArrayList());

	private static AttributeKey SESSION = AttributeKey.valueOf("SESSION");

	/**
	 * Instantiates a new client session filter.
	 * 
	 * @param mixinFactory
	 *            the mixin factory
	 */
	public ClientSessionFilter(ChannelPipelineFactory mixinFactory)
	{
		_mixinFactory = mixinFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected
	 * (org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		String id = _session == null ? "?" : _session.getId();
		// send the session id to server
		ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(id.getBytes()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(
	 * org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	public void messageReceived(ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		// if session established forward all messages
		if (_hasSession)
			ctx.fireChannelRead(e);
		else
		{
			ByteBuf b = (ByteBuf) e;
			_sessionId += b.toString(Charset.forName("UTF-8"));
			checkSession(ctx);
		}
	}

	private void checkSession(ChannelHandlerContext ctx)
	{
		if (_sessionId.length() == _factory.getSessionIdLength() * 2)
		{
			if (_session == null)
				newSession(ctx);
			else if (_session.getId().equals(_sessionId))
				confirmSession(ctx);
			else
				changedSession(ctx);
		}

	}

	private void changedSession(ChannelHandlerContext ctx)
	{
		closeSession(_session);
		newSession(ctx);
	}

	private void closeSession(Session session)
	{
		for (Runnable listener : _sessionClosedListeners)
		{
			try
			{
				listener.run();
			}
			catch (Throwable e)
			{
				Constants.ahessianLogger.warn("", e);
			}
		}
		HandlerList p = _sessionPipelines.remove(session.getId());

	}

	private void confirmSession(ChannelHandlerContext ctx)
	{
		HandlerList pipeline = _sessionPipelines.get(_session.getId());
		handleSession(ctx, pipeline);
	}

	private void newSession(ChannelHandlerContext ctx)
	{
		_session = _factory.createSession(_sessionId);
		HandlerList pipeline = null;
		try
		{
			pipeline = _mixinFactory.getPipeline();
			_sessionPipelines.put(_session.getId(), pipeline);
		}
		catch (Exception e)
		{
			Constants.ahessianLogger.warn("", e);
		}
		handleSession(ctx, pipeline);
		for (Runnable listener : _sessionNewListeners)
		{
			try
			{
				listener.run();
			}
			catch (Throwable ex)
			{
				Constants.ahessianLogger.warn("", ex);

			}
		}
	}

	private void handleSession(ChannelHandlerContext ctx, HandlerList pipeline)
	{
		_hasSession = true;
		// now that we have a session extend the pipeline
		pipeline.mixin(ctx);
		ctx.channel().attr(SESSION).set(_session);
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		_hasSession = false;
		_sessionId = "";
		ctx.fireChannelInactive();
	}

	public void addSessionClosedListener(Runnable listener)
	{
		_sessionClosedListeners.add(listener);
	}

	public void removeSessionClosedListener(Runnable listener)
	{
		_sessionClosedListeners.remove(listener);
	}

	public void addSessionNewListener(Runnable listener)
	{
		_sessionNewListeners.add(listener);
	}

}
