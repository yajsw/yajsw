package org.rzo.netty.ahessian.bootstrap;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public interface ChannelPipelineFactoryFactory
{
	public ChannelPipelineFactory create(EventExecutorGroup group, AbstractBootstrap bootstrap);

}
