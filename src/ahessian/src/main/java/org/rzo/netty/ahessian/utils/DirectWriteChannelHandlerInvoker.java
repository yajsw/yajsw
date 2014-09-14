package org.rzo.netty.ahessian.utils;

import static io.netty.channel.ChannelHandlerInvokerUtil.invokeWriteNow;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelHandlerInvoker;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.EventExecutor;

public class DirectWriteChannelHandlerInvoker extends DefaultChannelHandlerInvoker
{

	public DirectWriteChannelHandlerInvoker(EventExecutor executor)
	{
		super(executor);
	}
	
    @Override
    public void invokeWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg == null) {
            throw new NullPointerException("msg");
        }

        validatePromise(ctx, promise, true);

        invokeWriteNow(ctx, msg, promise);
        }

    private static void validatePromise(ChannelHandlerContext ctx, ChannelPromise promise, boolean allowVoidPromise) {
        if (ctx == null) {
            throw new NullPointerException("ctx");
        }

        if (promise == null) {
            throw new NullPointerException("promise");
        }

        if (promise.isDone()) {
            throw new IllegalArgumentException("promise already done: " + promise);
        }

        if (promise.channel() != ctx.channel()) {
            throw new IllegalArgumentException(String.format(
                    "promise.channel does not match: %s (expected: %s)", promise.channel(), ctx.channel()));
        }

        if (promise.getClass() == DefaultChannelPromise.class) {
            return;
        }

    }



}
