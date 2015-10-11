package io.netty.util.internal.logging;

public class SimpleLoggerFactory extends InternalLoggerFactory
{

	@Override
	public InternalLogger newInstance(String name)
	{
		return new SimpleLogger();
	}

}
