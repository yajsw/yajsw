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

package org.rzo.yajsw.nettyutils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Class WhitelistFilter.
 */
public class WhitelistFilter extends ChannelInboundHandlerAdapter
{

	/** The whitelist. */
	private final List<InetAddress> whitelist = new CopyOnWriteArrayList<InetAddress>();

	/**
	 * Sets the addresses to be whitelisted.
	 * 
	 * NOTE: this call will remove any previously blacklisted addresses.
	 * 
	 * @param addresses
	 *            an array of addresses to be blacklisted.
	 */
	public void allowAll(InetAddress[] addresses)
	{
		if (addresses == null)
		{
			throw new NullPointerException("addresses");
		}
		for (int i = 0; i < addresses.length; i++)
		{
			InetAddress addr = addresses[i];
			allow(addr);
		}
	}

	/**
	 * Sets the addresses to be blacklisted.
	 * 
	 * NOTE: this call will remove any previously blacklisted addresses.
	 * 
	 * @param addresses
	 *            a collection of InetAddress objects representing the addresses
	 *            to be blacklisted.
	 * 
	 * @throws IllegalArgumentException
	 *             if the specified collections contains non-{@link InetAddress}
	 *             objects.
	 */
	public void allowAll(Iterable<InetAddress> addresses)
	{
		if (addresses == null)
		{
			throw new NullPointerException("addresses");
		}

		for (InetAddress address : addresses)
		{
			allow(address);
		}
	}

	/**
	 * Blocks the specified endpoint.
	 * 
	 * @param address
	 *            the address
	 */
	public void allow(InetAddress address)
	{
		whitelist.add(address);
	}

	/**
	 * Unblocks the specified endpoint.
	 * 
	 * @param address
	 *            the address
	 */
	public void remove(InetAddress address)
	{
		if (address == null)
		{
			throw new NullPointerException("address");
		}
		whitelist.remove(address);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		if (!isBlocked(ctx.channel()))
		{
			// forward if not blocked
			ctx.fireChannelActive();
		}
		else
		{
			System.out.println("connection refused : "
					+ ctx.channel().remoteAddress());
			blockSession(ctx.channel());
		}
	}

	/**
	 * Block session.
	 * 
	 * @param session
	 *            the session
	 */
	private void blockSession(Channel session)
	{
		SocketAddress remoteAddress = session.remoteAddress();
		// logger.warn("Remote address " + remoteAddress +
		// " not in the whitelist; closing.");
		session.close();
	}

	/**
	 * Checks if is blocked.
	 * 
	 * @param session
	 *            the session
	 * 
	 * @return true, if is blocked
	 */
	private boolean isBlocked(Channel session)
	{
		SocketAddress remoteAddress = session.remoteAddress();
		if (remoteAddress instanceof InetSocketAddress)
		{
			if (whitelist.contains(((InetSocketAddress) remoteAddress)
					.getAddress()))
			{
				return false;
			}
		}

		return true;
	}
}
