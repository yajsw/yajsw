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
	 * @param ctx
	 *            the ChannelHandlerContext
	 * @param e
	 *            the MessageEvent
	 * 
	 * @return the state: NOT_COMPLETE, PASSED, FAILED
	 */
	public int authenticate(ChannelHandlerContext ctx, ByteBuf e);

	/**
	 * Send the password to the server
	 * 
	 * @param ctx
	 *            ChannelHandlerContext
	 */
	public void sendPassword(ChannelHandlerContext ctx);

	public boolean isLoggedOn();

	public void disconnected();

}
