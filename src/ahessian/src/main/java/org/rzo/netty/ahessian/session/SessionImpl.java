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
package org.rzo.netty.ahessian.session;

import io.netty.util.Timeout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rzo.netty.ahessian.Constants;

/**
 * The Class SessionImpl.
 */
class SessionImpl implements Session
{

	private String _id;

	private List<Runnable> _closedListeners = Collections
			.synchronizedList(new ArrayList<Runnable>());
	private List<Runnable> _invalidatedListeners = Collections
			.synchronizedList(new ArrayList<Runnable>());

	private Map<String, Object> _attributes = new HashMap<String, Object>();

	private long _created = System.currentTimeMillis();
	private volatile long _connected = System.currentTimeMillis();

	private volatile boolean _new = true;

	private volatile Timeout _timeout = null;

	private volatile boolean _valid = true;

	private volatile boolean _closed = false;

	private volatile long _messageCount = 0;

	/**
	 * Instantiates a new session impl.
	 * 
	 * @param id
	 *            the id
	 */
	SessionImpl(String id)
	{
		_id = id;
		_created = System.currentTimeMillis();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see handler.session.Session#getId()
	 */

	public String getId()
	{
		return _id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see handler.session.Session#addClosedListener(java.lang.Runnable)
	 */

	public void addClosedListener(Runnable listener)
	{
		_closedListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see handler.session.Session#close()
	 */

	public void close()
	{
		_closed = true;
		_connected = -1;
		synchronized (_closedListeners)
		{
			for (Runnable listener : _closedListeners)
				try
				{
					listener.run();
				}
				catch (Throwable ex)
				{
					Constants.ahessianLogger
							.warn(" execption in closedListener in session: "
									+ _id + " " + ex);
				}
		}
	}

	public void addInvalidatedListener(Runnable listener)
	{
		_invalidatedListeners.add(listener);
	}

	public Object getAttribute(String name)
	{
		return _attributes.get(name);
	}

	public Collection<String> getAttributeNames()
	{
		return new ArrayList(_attributes.keySet());
	}

	public long getCreationTime()
	{
		return _created;
	}

	public long getLastConnectedTime()
	{
		return _connected;
	}

	public void invalidate()
	{
		_valid = false;
		synchronized (_invalidatedListeners)
		{
			for (Runnable listener : _invalidatedListeners)
				try
				{
					listener.run();
				}
				catch (Throwable ex)
				{
					Constants.ahessianLogger
							.warn(" execption in invalidatedListener in session: "
									+ _id + " " + ex);
				}
		}
	}

	public boolean isNew()
	{
		return _new;
	}

	public void removeAttribute(String name)
	{
		_attributes.remove(name);
	}

	public void setAttribute(String name, Object value)
	{
		_attributes.put(name, value);
	}

	public void setNew(boolean newValue)
	{
		_new = newValue;
	}

	public void setTimeOut(Timeout timeOut)
	{
		_timeout = timeOut;
	}

	public Timeout removeTimeout()
	{
		Timeout result = _timeout;
		_timeout = null;
		return result;
	}

	public boolean isValid()
	{
		return _valid;
	}

	public void onMessage()
	{
		_messageCount++;
	}

	public void onConnected()
	{
		_connected = System.currentTimeMillis();
	}

	public long getMessageCount()
	{
		return _messageCount;
	}

	public boolean isClosed()
	{
		return _closed;
	}

	public void setClosed(boolean b)
	{
		_closed = b;
	}

}