package org.rzo.netty.ahessian.application.jmx.remote.server;

import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.log.OutLogger;
import org.rzo.netty.ahessian.session.ServerSessionFilter;

public class RPCServerSessionPipelineFactory extends ChannelPipelineFactory
{

	ChannelPipelineFactory _mixinFactory;
	
	RPCServerSessionPipelineFactory(ChannelPipelineFactory mixinFactory)
	{
		_mixinFactory = mixinFactory;
	}
	
	public HandlerList getPipeline() throws Exception
	{	
		HandlerList pipeline = new HandlerList();
    pipeline.addLast("logger",new OutLogger("1"));
    pipeline.addLast("sessionFilter", new ServerSessionFilter(_mixinFactory));
    return pipeline;
	}

}
