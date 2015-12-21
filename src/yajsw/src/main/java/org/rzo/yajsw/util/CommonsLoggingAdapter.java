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
package org.rzo.yajsw.util;

import io.netty.util.internal.logging.InternalLogger;

import org.apache.commons.logging.Log;

public class CommonsLoggingAdapter implements Log
{
	InternalLogger _log;

	public CommonsLoggingAdapter(InternalLogger log)
	{
		_log = log;
	}

	public void debug(Object arg0)
	{
		if (arg0 != null)
			_log.debug(arg0.toString());
		else
			_log.debug("null");
	}

	public void debug(Object arg0, Throwable arg1)
	{
		if (arg1 == null)
			return;
		if (arg0 != null)
			_log.debug(arg0.toString(), arg1);
		else
			_log.debug("null", arg1);
	}

	public void error(Object arg0)
	{
		if (arg0 != null)
			_log.error(arg0.toString());
		else
			_log.error("null");
	}

	public void error(Object arg0, Throwable arg1)
	{
		if (arg1 == null)
			return;
		if (arg0 != null)
			_log.error(arg0.toString(), arg1);
		else
			_log.error("null", arg1);
	}

	public void fatal(Object arg0)
	{
		error(arg0);
	}

	public void fatal(Object arg0, Throwable arg1)
	{
		error(arg0, arg1);
	}

	public void info(Object arg0)
	{
		if (arg0 != null)
			_log.info(arg0.toString());
		else
			_log.info("null");
	}

	public void info(Object arg0, Throwable arg1)
	{
		if (arg1 == null)
			return;
		if (arg0 != null)
			_log.info(arg0.toString(), arg1);
		else
			_log.info("null", arg1);
	}

	public boolean isDebugEnabled()
	{
		return false;
	}

	public boolean isErrorEnabled()
	{
		return _log.isErrorEnabled();
	}

	public boolean isFatalEnabled()
	{
		return _log.isErrorEnabled();
	}

	public boolean isInfoEnabled()
	{
		return _log.isInfoEnabled();
	}

	public boolean isTraceEnabled()
	{
		return _log.isDebugEnabled();
	}

	public boolean isWarnEnabled()
	{
		return _log.isWarnEnabled();
	}

	public void trace(Object arg0)
	{
		debug(arg0);
	}

	public void trace(Object arg0, Throwable arg1)
	{
		debug(arg0, arg1);
	}

	public void warn(Object arg0)
	{
		if (arg0 != null)
			_log.warn(arg0.toString());
		else
			_log.warn("null");
	}

	public void warn(Object arg0, Throwable arg1)
	{
		if (arg1 == null)
			return;
		if (arg0 != null)
			_log.warn(arg0.toString(), arg1);
		else
			_log.warn("null", arg1);
	}

}
