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
package org.rzo.netty.ahessian.application.cluster.service;

import java.util.List;

import org.rzo.netty.ahessian.rpc.callback.Callback;

public interface ClusterService
{
	public interface Member
	{
		public String getName();

		public String getHost();

		public Object getData();

		public long getConnectionTime();

		public Member getClusterServer();

		boolean isSeed();
	}

	public interface ClusterEventListener extends Callback
	{
		public void joined(Member member);

		public void left(Member member);
	}

	public void join(String clusterName, String clientName, Object data,
			ClusterEventListener listener);

	public void leave(String clientName);

	public List<Member> getMembers();

}
