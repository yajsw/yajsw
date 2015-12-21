package io.netty.util.internal.logging;

public class SimpleErrorLoggerFactory extends InternalLoggerFactory
{
	@Override
	public InternalLogger newInstance(String name)
	{
		return new SimpleErrorLogger();
	}

}
