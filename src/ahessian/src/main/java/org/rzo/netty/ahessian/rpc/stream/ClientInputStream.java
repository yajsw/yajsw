package org.rzo.netty.ahessian.rpc.stream;

import io.netty.buffer.Unpooled;

import java.io.IOException;

import org.rzo.netty.ahessian.io.InputStreamBuffer;

public class ClientInputStream extends InputStreamBuffer
{
	private long _id;

	public ClientInputStream(long id)
	{
		_id = id;
	}
	
	public void addMessage(InputStreamReplyMessage msg)
	{
		if (msg.isClosed())
			try
			{
				closeOnEmpty();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		else
			try
			{
				this.write(Unpooled.wrappedBuffer(msg.getData()));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
	}

	public long getId()
	{
		return _id;
	}


}
