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
package org.rzo.netty.ahessian.rpc.callback;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.rpc.message.HessianRPCCallMessage;
import org.rzo.netty.ahessian.rpc.message.HessianRPCReplyMessage;

public class CallbackReplyMessage extends HessianRPCReplyMessage implements
		Constants
{

	private Object[] _args;
	private String _method;
	private boolean _done = false;

	public CallbackReplyMessage(String method, Object[] args, Object fault,
			HessianRPCCallMessage message)
	{
		super(null, fault, message);
		_args = args;
		_method = method;
	}

	public void setDone(Boolean done)
	{
		_done = done;
	}

	public Object[] getArgs()
	{
		return _args;
	}

	public String getMethod()
	{
		return _method;
	}

	public boolean isDone()
	{
		return _done;
	}

}
