package org.rzo.netty.mcast.bridge;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.mcast.MulticastEndpoint;

public class MulticastAccessPoint
{
	
	private static List<Channel> remoteChannels = Collections.synchronizedList(new ArrayList<Channel>());
	private static MulticastEndpoint mcast = new MulticastEndpoint();

	public static void main(String[] args)
	{
		int port = Integer.parseInt(args[0]);
		
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class);
        
        bootstrap.childHandler(new ChannelPipelineFactory() {
            public HandlerList getPipeline() {
                return ChannelPipelineFactory.handlerList(new ChannelInboundHandlerAdapter()
                {

                	
    				@Override
    				public void channelRead(ChannelHandlerContext ctx, Object msg)
    						throws Exception
    				{
    					if (mcast != null && mcast.isInit())
    						mcast.send((ByteBuf) msg);
    				}
    				
    				@Override
    			    public void channelActive(ChannelHandlerContext ctx) throws Exception
    				{
    					remoteChannels.add(ctx.channel());
    			    }
    				
    				@Override
    			    public void channelInactive(ChannelHandlerContext ctx)  throws Exception
    				{
    					remoteChannels.add(ctx.channel());
    			    }
    				
    				@Override
    			    public void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable e)
    			    	    throws Exception
    			    	{
    			        Throwable cause = e.getCause();
    			        System.out.println(e);    				
    			    }
    			    
                });
            }
        });
        try
		{
			bootstrap.bind(new InetSocketAddress(port)).sync();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
        
        try
		{
			mcast.init(new ChannelPipelineFactory() {
			    public HandlerList getPipeline() {
			        return ChannelPipelineFactory.handlerList(new ChannelInboundHandlerAdapter()
			        {
						@Override
						public void channelRead(ChannelHandlerContext ctx, Object msg)
	    						throws Exception
						{
							ByteBuf b = mcast.getMessage((ByteBuf)msg);
							if (b == null)
								return;
							for (Channel c : remoteChannels)
							{
								if (c.isActive())
									c.write(b);
							}
						}
			        	
			        });
			    }
			});
		}
		catch (Exception e)
		{
			Constants.ahessianLogger.warn("", e);
		}

	}

}
