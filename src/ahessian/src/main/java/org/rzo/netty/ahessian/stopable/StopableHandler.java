package org.rzo.netty.ahessian.stopable;

import io.netty.channel.ChannelHandler;


public interface StopableHandler extends ChannelHandler
{
	public void stop();
	public boolean isStopEnabled();
	public void setStopEnabled(boolean stopEnabled);
}
