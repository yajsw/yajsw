package org.rzo.netty.ahessian.auth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;


/**
 * The Interface AuthToken.
 */
public interface AuthToken
{
	
	/** Password not completely received */
	public static int NOT_COMPLETE = 0;
	
	/** Authentication passed */
	public static int PASSED = 1;
	
	/** Authentication failed */
	public static int FAILED = 2;
	
	/**
	 * Authenticate the received password
	 * 
	 * @param ctx the ChannelHandlerContext
	 * @param e the MessageEvent
	 * 
	 * @return the state: NOT_COMPLETE, PASSED, FAILED
	 */
	public int authenticate(ChannelHandlerContext ctx, ByteBuf e);
	
	/**
	 * Send the password to the server
	 * 
	 * @param ctx ChannelHandlerContext
	 */
	public void sendPassword(ChannelHandlerContext ctx);

	public boolean isLoggedOn();

	public void disconnected();
	
}
