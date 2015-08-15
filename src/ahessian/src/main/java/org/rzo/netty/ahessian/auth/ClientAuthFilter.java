package org.rzo.netty.ahessian.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

/**
 * Client side authentication handler.
 * <br>
 * This must be the first handler in the pipeline.
 * 
 *  <br>
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
	 * @param token the token
	 */
	public ClientAuthFilter(AuthToken token)
	{
		_token = token;
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
