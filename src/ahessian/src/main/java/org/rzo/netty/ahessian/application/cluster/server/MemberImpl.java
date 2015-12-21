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
		return "[Member name:" + _name + ", Server:" + _server + "]";
	}

}
