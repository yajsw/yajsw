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
	
	public void join(String clusterName, String clientName, Object data, ClusterEventListener listener);
	public void leave(String clientName);
	public List<Member> getMembers();

}
