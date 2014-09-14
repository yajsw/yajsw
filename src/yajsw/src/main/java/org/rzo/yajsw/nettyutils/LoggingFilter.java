package org.rzo.yajsw.nettyutils;

import io.netty.handler.logging.LoggingHandler;

import java.util.logging.Logger;

public class LoggingFilter extends LoggingHandler
{
	Logger	_logger;
	String	_name;

	public LoggingFilter(Logger logger, String name)
	{
		_logger = logger;
		_name = name;
	}


	private void log(String txt)
	{
		if (_logger == null)
			System.out.println(txt);
		else
			_logger.fine("[" + _name + "]" + txt);
	}

}
