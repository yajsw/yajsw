package org.rzo.yajsw.util;

import org.apache.commons.configuration2.io.ConfigurationLogger;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.SimpleLogger;

public class ConfigurationLoggingAdapter extends ConfigurationLogger
{
	InternalLogger _log;

	public ConfigurationLoggingAdapter(final InternalLogger log)
	{
		_log = log;
	}
	
	private InternalLogger getILog()
	{
		return _log;
	}
	
	
	
    /**
     * Returns a flag whether logging on debug level is enabled.
     *
     * @return <b>true</b> if debug logging is enabled, <b>false</b> otherwise
     */
    public boolean isDebugEnabled()
    {
        return getILog().isDebugEnabled();
    }

    /**
     * Logs the specified message on debug level.
     *
     * @param msg the message to be logged
     */
    public void debug(String msg)
    {
    	getILog().debug(msg);
    }

    /**
     * Returns a flag whether logging on info level is enabled.
     *
     * @return <b>true</b> if debug logging is enabled, <b>false</b> otherwise
     */
    public boolean isInfoEnabled()
    {
        return getILog().isInfoEnabled();
    }

    /**
     * Logs the specified message on info level.
     *
     * @param msg the message to be logged
     */
    public void info(String msg)
    {
    	getILog().info(msg);
    }

    /**
     * Logs the specified message on warn level.
     *
     * @param msg the message to be logged
     */
    public void warn(String msg)
    {
        getILog().warn(msg);
    }

    /**
     * Logs the specified exception on warn level.
     *
     * @param msg the message to be logged
     * @param ex the exception to be logged
     */
    public void warn(String msg, Throwable ex)
    {
        getILog().warn(msg, ex);
    }

    /**
     * Logs the specified message on error level.
     *
     * @param msg the message to be logged
     */
    public void error(String msg)
    {
        getILog().error(msg);
    }

    /**
     * Logs the specified exception on error level.
     *
     * @param msg the message to be logged
     * @param ex the exception to be logged
     */
    public void error(String msg, Throwable ex)
    {
        getILog().error(msg, ex);
    }

 	
	public static void main(String[] args)
	{
		ConfigurationLogger l = new ConfigurationLoggingAdapter(new SimpleLogger());
		l.info("tewste");
	}


}
