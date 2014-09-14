package org.rzo.netty.ahessian.application.jmx.remote.server;

import io.netty.channel.EventLoopGroup;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;

import org.rzo.netty.ahessian.application.jmx.remote.service.JmxSerializerFactory;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.io.InputStreamHandler;
import org.rzo.netty.ahessian.io.OutputStreamHandler;
import org.rzo.netty.ahessian.io.PullInputStreamConsumer;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallDecoder;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyEncoder;
import org.rzo.netty.ahessian.rpc.server.HessianRPCServiceHandler;
import org.rzo.netty.ahessian.rpc.server.ImmediateInvokeService;

import com.caucho.hessian4.io.SerializerFactory;

public class RPCServerMixinPipelineFactory extends ChannelPipelineFactory
{
	
	Executor _executor;
	SerializerFactory _serializerFactory = new JmxSerializerFactory();

	RPCServerMixinPipelineFactory(Executor executor, EventLoopGroup group)
	{
		super(group);
		_executor = executor;
	}
	
	public HandlerList getPipeline() throws Exception
	{
		HandlerList pipeline = new HandlerList();
	    //ChannelHandlerInvoker invoker = new DirectWriteChannelHandlerInvoker(getGroup().next());
        pipeline.addLast("inputStream", new InputStreamHandler());
        pipeline.addLast("callDecoder", new PullInputStreamConsumer(new HessianRPCCallDecoder(_serializerFactory)));
        pipeline.addLast("outputStream", new OutputStreamHandler(), getGroup());
        pipeline.addLast("replyEncoder", new HessianRPCReplyEncoder(_serializerFactory, _executor), getGroup());
        HessianRPCServiceHandler factory =  new HessianRPCServiceHandler(_executor);
		ArrayList servers = MBeanServerFactory.findMBeanServer(null);
		MBeanServer server = null;
			if (servers != null && servers.size() > 0)
				server = (MBeanServer) servers.get(0);
			if (server == null)
				server = MBeanServerFactory.createMBeanServer();
		
        factory.addService("default", new ImmediateInvokeService(server, MBeanServerConnection.class, factory));
        pipeline.addLast("hessianRPCServer", factory, getGroup());
        return pipeline;
	}

}
