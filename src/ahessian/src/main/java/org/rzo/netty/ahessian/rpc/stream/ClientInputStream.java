/*******************************************************************************
 * Copyright  2015 rzorzorzo@users.sf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
