package org.rzo.netty.ahessian.application.jmx.remote.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Server
{
    public static void main(String[] args)
    {
        Executor executor = Executors.newFixedThreadPool(200);
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, childGroup);
        bootstrap.channel(NioServerSocketChannel.class); 

        bootstrap.childHandler(
               new RPCServerSessionPipelineFactory( new RPCServerMixinPipelineFactory(executor, childGroup)));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8080));

    }

}
