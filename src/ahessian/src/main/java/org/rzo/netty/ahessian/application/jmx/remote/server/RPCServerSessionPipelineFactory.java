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
package org.rzo.netty.ahessian.application.jmx.remote.server;

import org.rzo.netty.ahessian.bootstrap.ChannelPipelineFactory;
import org.rzo.netty.ahessian.log.OutLogger;
import org.rzo.netty.ahessian.session.ServerSessionFilter;

public class RPCServerSessionPipelineFactory extends ChannelPipelineFactory
{

	ChannelPipelineFactory _mixinFactory;

	RPCServerSessionPipelineFactory(ChannelPipelineFactory mixinFactory)
	{
		_mixinFactory = mixinFactory;
	}

	public HandlerList getPipeline() throws Exception
	{
		HandlerList pipeline = new HandlerList();
		pipeline.addLast("logger", new OutLogger("1"));
		pipeline.addLast("sessionFilter",
				new ServerSessionFilter(_mixinFactory));
		return pipeline;
	}

}
