package org.rzo.netty.ahessian.application.jmx.remote.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.ConnectException;
import java.util.Timer;
import java.util.TimerTask;

import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.log.OutLogger;
import org.rzo.netty.ahessian.session.ClientSessionFilter;

public class RPCClientSessionPipelineFactory extends ChannelPipelineFactory
{

	ChannelPipelineFactory _mixinFactory;
	ClientSessionFilter _sessionFilter;
	private static Timer timer = new Timer();
	private static long RECONNECT_DELAY = 5000;
	Bootstrap _bootstrap;


	
	RPCClientSessionPipelineFactory(ChannelPipelineFactory mixinFactory, Bootstrap bootstrap)
	{
		_mixinFactory = mixinFactory;
		_sessionFilter = new ClientSessionFilter(_mixinFactory);
		_bootstrap = bootstrap;
	}
	
	public HandlerList getPipeline() throws Exception
	{	
		HandlerList pipeline = new HandlerList();
    pipeline.addLast("logger",new OutLogger("1"));
    pipeline.addLast("reconnector", new ChannelHandlerAdapter()
                {
    				
    				@Override
    			    public void channelInactive(ChannelHandlerContext ctx) {
    					ctx.fireChannelInactive();
    					System.out.println("channel closed wait to reconnect ...");
    			        timer.schedule(new TimerTask() {
    			            public void run() {
    			            	System.out.println("reconnecting...");
     			              ChannelFuture f = _bootstrap.connect();
     			              try
							{
     			            	  System.out.println("future wait");
								f.awaitUninterruptibly();
   			            	  System.out.println("future wait terminated");
							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
	 							}
   			              if (f.isSuccess())
 			            	  System.out.println("connected");
   			              else
   			              {
   			            	  System.out.println("not connected");
   			            	 // f.getChannel().close();
   			              }
    			               
    			            }
    			        }, RECONNECT_DELAY);
    			    }
    				
    				@Override
    			    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
    			        Throwable cause = e;
    			        if (cause instanceof ConnectException) 
    			        {
    			        	System.out.println("conection lost");
    			        }
    			        ctx.channel().close();
    			    }
                }
);
    pipeline.addLast("sessionFilter", _sessionFilter);

    return pipeline;
	}

}
