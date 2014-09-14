package org.rzo.netty.ahessian.application.cluster.server;

import org.rzo.netty.ahessian.application.cluster.service.ClusterService;

public interface SeedClusterService extends ClusterService
{
	public void seedJoin(Member seed, ClusterEventListener listener);
}
