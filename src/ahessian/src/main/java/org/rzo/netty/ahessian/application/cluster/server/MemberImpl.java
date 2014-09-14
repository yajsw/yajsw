package org.rzo.netty.ahessian.application.cluster.server;

import java.io.Serializable;

import org.rzo.netty.ahessian.application.cluster.service.ClusterService.Member;

public class MemberImpl implements Member, Serializable
{
	String _name;
	String _host;
	Object _data;
	long _connectionTime = System.currentTimeMillis();
	Member _server;
	boolean _isSeed = false;

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public String getHost()
	{
		return _host;
	}

	@Override
	public Object getData()
	{
		return _data;
	}

	@Override
	public long getConnectionTime()
	{
		return _connectionTime;
	}

	@Override
	public Member getClusterServer()
	{
		return _server;
	}
	
	@Override
	public boolean isSeed()
	{
		return _isSeed;
	}
	
	@Override
	public String toString()
	{
		return "[Member name:"+_name+", Server:"+_server+"]";
	}

}
