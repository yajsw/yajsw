package org.rzo.netty.ahessian.heartbeat;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.atomic.AtomicLong;

import org.rzo.netty.ahessian.Constants;

public class HeartbeatHandlerInbound extends ChannelInboundHandlerAdapter
{
	volatile AtomicLong _lastCalled = new AtomicLong();
	volatile ChannelHandlerContext _ctx;
	final IntervalTimer _intervalTimer;
	final String _name;
	TimedOutAction _action;

	public HeartbeatHandlerInbound(final String name, final Timer timer, final long timeout)
	{
		_name = name;
		final TimerTask task = new TimerTask()
		{
			public void run(Timeout nTimeout) throws Exception
			{
				if (((getLastCalled() + timeout) <= System.currentTimeMillis()) && isConnected())
					try
					{
						_action.timedOut(_ctx);
					}
					catch (Exception e)
					{
						Constants.ahessianLogger.warn("", e);
					}
			}
			
		};
		_intervalTimer = new IntervalTimer(timer, task, timeout);
	}
	
    long getLastCalled()
    {
    	return _lastCalled.get();
    }
    
    boolean isConnected()
    {
    	return _ctx != null && _ctx.channel().isActive();
    }
   
    @Override
    public void channelInactive(
            ChannelHandlerContext ctx) throws Exception 
            {
    _ctx = null;
    _intervalTimer.stop();
    ctx.fireChannelInactive();
    }
    
    protected void ping()
    {
		_lastCalled.set(System.currentTimeMillis());    	
    }

    @Override
    public void channelActive(
            ChannelHandlerContext ctx) throws Exception {
		ping();
		_ctx = ctx;
		_intervalTimer.setName(_name+":"+_ctx.channel().hashCode());
		
		Constants.ahessianLogger.info("AbstractHeartBeatHandler scheduler started: "+ _intervalTimer.getInterval());
		_intervalTimer.start();
		ctx.fireChannelActive();
    }

	public void setAction(TimedOutAction action)
	{
		_action = action;
	}



	
	

}
