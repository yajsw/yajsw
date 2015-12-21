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
package org.rzo.netty.ahessian.rpc.message;

import io.netty.channel.Channel;

import java.util.Map;

import org.rzo.netty.ahessian.Constants;
import org.rzo.netty.ahessian.io.InputStreamHandler;
import org.rzo.netty.ahessian.io.OutputStreamHandler;
import org.rzo.netty.ahessian.session.ServerSessionFilter;
import org.rzo.netty.ahessian.session.Session;

/**
 * message used for requesting a remote invocation
 */
public class HessianRPCCallMessage implements Constants, GroupedMessage
{

	/** The _method. */
	String _method;

	/** The _args. */
	Object[] _args;

	/** The _headers. */
	Map<Object, Object> _headers;
	transient OutputStreamHandler _outputStreamEncoder;
	boolean _hasSessionFilter = false;
	transient boolean _isServer = false;
	transient Session _session;
	transient InputStreamHandler _handler;

	/**
	 * Gets the headers.
	 * 
	 * @return the headers
	 */
	public Map<Object, Object> getHeaders()
	{
		return _headers;
	}

	/**
	 * Instantiates a new hessian rpc call message.
	 * 
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @param headers
	 *            the headers
	 */
	public HessianRPCCallMessage(String method, Object[] args,
			Map<Object, Object> headers, OutputStreamHandler outputStreamEncoder)
	{
		_method = method;
		_args = args;
		_headers = headers;
		_outputStreamEncoder = outputStreamEncoder;
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public String getMethod()
	{
		return _method;
	}

	/**
	 * Gets the args.
	 * 
	 * @return the args
	 */
	public Object[] getArgs()
	{
		return _args;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("HessianRPCCallMessage");
		if (_headers != null && _headers.get(CALL_ID_HEADER_KEY) != null)
		{
			sb.append('#');
			sb.append(_headers.get(CALL_ID_HEADER_KEY));
		}
		sb.append('[');
		sb.append(_method);
		sb.append('(');
		if (_args != null)
			for (int i = 0; i < _args.length; i++)
			{
				if (_args[i] != null)
					sb.append(_args[i].toString());
				else
					sb.append("null");
				if (i != _args.length - 1)
					sb.append(',');
			}
		sb.append(")]");
		return sb.toString();
	}

	public boolean isValid()
	{
		boolean result = false;
		if (!_hasSessionFilter || !_isServer)
			result = (_outputStreamEncoder != null
					&& _outputStreamEncoder.getBuffer() != null
					&& _outputStreamEncoder.getBuffer().getContext() != null && _outputStreamEncoder
					.getBuffer().getContext().channel().isActive());
		else if (_outputStreamEncoder != null
				&& _outputStreamEncoder.getBuffer() != null
				&& _outputStreamEncoder.getBuffer().getContext() != null)
		{
			ServerSessionFilter session = ServerSessionFilter
					.getServerSessionFilter(_outputStreamEncoder.getBuffer()
							.getContext());
			result = session == null || session.isValid();
		}
		return result;
	}

	public Channel getChannel()
	{
		if (_outputStreamEncoder != null
				&& _outputStreamEncoder.getBuffer() != null
				&& _outputStreamEncoder.getBuffer().getContext() != null)
			return _outputStreamEncoder.getBuffer().getContext().channel();
		return null;
	}

	public void setHasSessionFilter(boolean hasSessionFilter)
	{
		_hasSessionFilter = hasSessionFilter;
		_headers.put(HAS_SESSION_FILTER_HEADER_KEY, _hasSessionFilter);
	}

	public void setServer(boolean isServer)
	{
		_isServer = isServer;
	}

	public Integer getGroup()
	{
		if (_headers == null || _headers.get(GROUP_HEADER_KEY) == null)
			return 0;
		return (Integer) _headers.get(GROUP_HEADER_KEY);
	}

	public void setSession(Session session)
	{
		_session = session;
	}

	public Session getSession()
	{
		return _session;
	}

	public void setHandler(InputStreamHandler handler)
	{
		_handler = handler;
	}

	public InputStreamHandler getHandler()
	{
		return _handler;
	}

}
