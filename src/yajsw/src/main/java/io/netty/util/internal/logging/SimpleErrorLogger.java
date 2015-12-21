package io.netty.util.internal.logging;

public class SimpleErrorLogger extends SimpleLogger
{
	@Override
	public boolean isDebugEnabled()
	{
		return false;
	}

	@Override
	public boolean isInfoEnabled()
	{
		return false;
	}

	@Override
	public boolean isWarnEnabled()
	{
		return false;
	}

	@Override
	public void debug(String arg0)
	{
	}

	@Override
	public void info(String arg0)
	{
	}

	@Override
	public void warn(String arg0)
	{
	}

	@Override
	public void trace(String arg0)
	{
	}

	@Override
	public void trace(String paramString, Object paramObject)
	{
	}

	@Override
	public void trace(String paramString, Object paramObject1,
			Object paramObject2)
	{
	}

	@Override
	public void trace(String paramString, Object... paramArrayOfObject)
	{
	}

	public void debug(String paramString, Object paramObject)
	{
	}

	@Override
	public void debug(String paramString, Object paramObject1,
			Object paramObject2)
	{
	}

	@Override
	public void debug(String paramString, Object... paramArrayOfObject)
	{
	}

}
