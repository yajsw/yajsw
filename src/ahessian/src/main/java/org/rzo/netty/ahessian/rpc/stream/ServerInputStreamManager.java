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

import io.netty.channel.Channel;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ServerInputStreamManager
{
	long _id = 0;
	Map<Long, ServerInputStream> _streams = new HashMap<Long, ServerInputStream>();
	Executor _executor;

	public ServerInputStreamManager(Executor executor)
	{
		_executor = executor;
	}

	synchronized public ServerInputStream createServerInputStream(
			InputStream stream, Channel channel)
	{
		ServerInputStream result = new ServerInputStream(stream, _executor,
				channel, _id);
		synchronized (_streams)
		{
			_streams.put(_id, result);
		}
		_id++;
		return result;
	}

}
