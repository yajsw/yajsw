package org.rzo.netty.ahessian.bootstrap;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.EventLoopGroup;

public interface ChannelPipelineFactoryFactory
{
	public ChannelPipelineFactory create(EventLoopGroup group, AbstractBootstrap bootstrap);

}
