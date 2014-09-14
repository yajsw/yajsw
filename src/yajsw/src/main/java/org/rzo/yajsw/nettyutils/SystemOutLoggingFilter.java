package org.rzo.yajsw.nettyutils;

import io.netty.handler.logging.LoggingHandler;

public class SystemOutLoggingFilter extends LoggingHandler
{
	String	_name;

	public SystemOutLoggingFilter(String name)
	{
		_name = name;
	}

	private void log(String txt)
	{
			System.out.println("[" + _name + "]" + txt);
	}

}
