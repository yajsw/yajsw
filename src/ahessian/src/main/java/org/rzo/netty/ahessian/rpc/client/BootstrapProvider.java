package org.rzo.netty.ahessian.rpc.client;

import io.netty.bootstrap.AbstractBootstrap;


public interface BootstrapProvider
{
	public AbstractBootstrap getBootstrap();
}
