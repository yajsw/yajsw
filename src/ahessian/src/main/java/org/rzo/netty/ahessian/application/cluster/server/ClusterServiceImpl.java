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
package org.rzo.netty.ahessian.application.cluster.server;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rzo.netty.ahessian.rpc.server.HessianSkeleton;

public class ClusterServiceImpl implements SeedClusterService
{
	String _clusterName;
	Map<String, Member> _members = new ConcurrentHashMap<String, Member>();
	Map<String, Member> _joinedSeeds = new ConcurrentHashMap<String, Member>();
	Map<String, Channel> _channels = new ConcurrentHashMap<String, Channel>();
	Map<String, String> _channel2member = new ConcurrentHashMap<String, String>();
	Map<String, ClusterEventListener> _listners = new ConcurrentHashMap<String, ClusterEventListener>();
	Member _server;

	public void setServer(Member server)
	{
		_server = server;
		_members.put(_server.getName(), server);
	}

	public void setClusterName(String name)
	{
		_clusterName = name;
	}

	@Override
	synchronized public void join(String clusterName, String clientName,
			Object data, ClusterEventListener listener)
	{
		System.out.println("+join " + clientName + " "
				+ System.currentTimeMillis());
		Channel channel = getChannel();
		String host = getHost(channel);

		if (channel == null)
			throw new RuntimeException("join: could not get the channel for "
					+ _clusterName + "/" + clientName);

		Member newMember = createMember(clusterName, clientName, data, false,
				host);
		joinInternal(newMember);
		_channels.put(clientName, channel);
		_channel2member.put("" + channel.hashCode(), clientName);
		if (listener != null)
			_listners.put(clientName, listener);
		System.out.println("-join " + clientName);
	}

	private Channel getChannel()
	{
		Channel channel = HessianSkeleton.threadLocalChannel.get();
		return channel;
	}

	private String getHost(Channel channel)
	{
		InetSocketAddress remoteAddress = (InetSocketAddress) channel
				.remoteAddress();
		return remoteAddress.getHostName();
	}

	synchronized public void seedJoin(Member member,
			ClusterEventListener listener)
	{
		if (member.isSeed() && _joinedSeeds.containsKey(member.getName()))
			throw new RuntimeException("already joined " + _clusterName + "/"
					+ member.getName());

		if (member.isSeed())
		{
			Channel channel = getChannel();
			if (channel != null)
			{
				_joinedSeeds.put(member.getName(), member);
				_channels.put(member.getName(), channel);
				_channel2member.put("" + channel.hashCode(), member.getName());
			}
			if (listener != null)
				_listners.put(member.getName(), listener);
		}
		if (!_members.containsKey(member.getName()))
			joinInternal(member);
	}

	synchronized void channelClosed(Channel channel)
	{
		String name = _channel2member.remove("" + channel.hashCode());
		if (name == null)
			return;
		leave(name);
	}

	synchronized protected Member createMember(String clusterName,
			String clientName, Object data, boolean isSeed, String host)
	{
		if (_members.containsKey(clientName))
			throw new RuntimeException("already joined " + clusterName + "/"
					+ clientName);
		if (!_clusterName.equals(clusterName))
			throw new RuntimeException("cannot join wrong cluster "
					+ _clusterName + "!=" + clusterName + "/" + clientName);
		MemberImpl newMember = new MemberImpl();
		newMember._data = data;
		newMember._host = host;
		newMember._name = clientName;
		newMember._server = _server;
		newMember._isSeed = isSeed;
		return newMember;
	}

	private void joinInternal(Member newMember)
	{
		_members.put(newMember.getName(), newMember);
		announceJoin(newMember);
	}

	@Override
	synchronized public void leave(String clientName)
	{
		Member member = _members.remove(clientName);
		if (member == null)
			throw new RuntimeException("cannot leave not joined cluster "
					+ _clusterName + "/" + clientName);
		_listners.remove(clientName);
		announceLeave(member);
		Channel channel = _channels.remove(clientName);
		if (channel != null)
		{
			channel.close();
			_channel2member.remove("" + channel.hashCode());
		}
		if (member.isSeed())
			seedLeft(member);
	}

	private void seedLeft(Member seed)
	{
		_joinedSeeds.remove(seed.getName());
		for (Member member : _members.values())
			if (seed.equals(member.getClusterServer()))
				removeRemoteMember(member);
	}

	private void removeRemoteMember(Member member)
	{
		String clientName = member.getName();
		leave(clientName);
	}

	@Override
	public List<Member> getMembers()
	{
		return new ArrayList(_members.values());
	}

	private void announceLeave(Member member)
	{
		String clientName = member.getName();
		for (String client : _listners.keySet())
		{
			if (!clientName.equals(client))
			{
				ClusterEventListener listner = _listners.get(clientName);
				if (listner != null)
					try
					{
						listner.left(member);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
			}
		}
	}

	private void announceJoin(Member member)
	{
		String clientName = member.getName();
		for (String client : _listners.keySet())
		{
			if (!clientName.equals(client))
			{
				ClusterEventListener listner = _listners.get(clientName);
				if (listner != null)
					try
					{
						listner.joined(member);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
			}
		}
	}

	public void disconnected(Channel channel)
	{
		String client = _channel2member.remove("" + channel.hashCode());
		if (client != null)
			leave(client);
	}

}
