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
package org.rzo.yajsw.nettyutils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConditionFilter extends ChannelInboundHandlerAdapter
{

	Condition _condition;

	public ConditionFilter(Condition condition)
	{
		_condition = condition;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		if (_condition.isOk(ctx, null))
		{
			// forward if condtion met
			ctx.fireChannelActive();
		}
		else
		{
			ctx.channel().close();
		}
	}

}
