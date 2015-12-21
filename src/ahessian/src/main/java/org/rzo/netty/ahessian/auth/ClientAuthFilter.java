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
package org.rzo.netty.ahessian.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

/**
 * Client side authentication handler. <br>
 * This must be the first handler in the pipeline.
 * 
 * <br>
 * A typical setup for ClientAuthFilter for TCP/IP socket would be:
 * 
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 * 
 *   EncryptedAuthToken token = new EncryptedAuthToken();
 *   token.setAlgorithm("SHA-1");
 *   token.setPassword("test");
 *   ClientAuthFilter auth = new ClientAuthFilter(token);
 *   pipeline.addLast("auth", auth);
 * </pre>
 * 
 */
public class ClientAuthFilter extends ChannelInboundHandlerAdapter
{

	/** The authentication token. */
	AuthToken _token;

	/**
	 * Instantiates a new client authentication handler.
	 * 
	 * @param token
	 *            the token
	 */
	public ClientAuthFilter(AuthToken token)
	{
		_token = token;
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
		_token.sendPassword(ctx);
		ctx.fireChannelActive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception
	{
		ctx.fireChannelRead(msg);
	}

}
