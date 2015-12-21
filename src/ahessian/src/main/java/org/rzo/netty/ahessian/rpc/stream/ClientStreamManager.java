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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ClientStreamManager
{
	Map<Long, ClientInputStream> _streams = new HashMap<Long, ClientInputStream>();

	public synchronized InputStream newInputStream(long id)
	{
		ClientInputStream result = new ClientInputStream(id);
		_streams.put(id, result);
		return result;
	}

	public synchronized void removeInputStream(ClientInputStream stream)
	{
		_streams.remove(stream.getId());
	}

	public synchronized void messageReceived(InputStreamReplyMessage msg)
	{
		ClientInputStream stream;
		if (msg.isClosed())
			stream = _streams.remove(msg.getId());
		else
			stream = _streams.get(msg.getId());
		if (stream != null)
			stream.addMessage(msg);
		else
			System.out
					.println("message for non existing stream " + msg.getId());
	}

}
