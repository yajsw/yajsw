package org.rzo.yajsw.os.posix;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.rzo.yajsw.util.DaemonThreadFactory;

public class PosixUtils
{
	protected static final Executor	executor	= Executors.newCachedThreadPool(new DaemonThreadFactory("util.osCommand"));
	protected Logger				_logger;

	public void setLog(Logger logger)
	{
		_logger = logger;
	}
	
	public String readFile(String file)
	{
		String result = "";
		File f = new File(file);
		if (f.exists())
			try
			{
				InputStream in = new FileInputStream(f);
				byte[] buffer = new byte[10 * 1024];
				int size = 0;
				String rest = "";
				
				while ((size = in.read(buffer)) > 0)
				{
				// System.out.println("size "+size);
				for (int i = 0; i < size; i++)
					if (buffer[i] == 0)
					{
						buffer[i] = (byte)' ';
					}
				result += new String(buffer, 0, size);
				}
				in.close();
			}
			catch (Exception e)
			{
				if (_logger != null)
					_logger.throwing(PosixUtils.class.getName(), "readFile", e);
			}
		else
		{
			if (_logger != null)
				_logger.info("could not find file " + f.getAbsolutePath());
			// throw new NullPointerException();
		}
		return result;

	}


	public String readFileQuoted(String file)
	{
		List<String> result = new ArrayList<String>();
		File f = new File(file);
		if (f.exists())
			try
			{
				InputStream in = new FileInputStream(f);
				byte[] buffer = new byte[10 * 1024];
				int size = 0;
				String rest = "";
				
				while ((size = in.read(buffer)) > 0)
				{
				// System.out.println("size "+size);
				int k = 0;
				for (int i = 0; i < size; i++)
					if (buffer[i] == 0)
					{
						result.add(rest + new String(buffer, k, i-k));
						rest = "";
						k = i+1;
					}
				rest = new String(buffer, k, size);
				}
				if (rest.length() > 0)
					result.add(rest);
				in.close();
			}
			catch (Exception e)
			{
				if (_logger != null)
					_logger.throwing(PosixUtils.class.getName(), "readFile", e);
			}
		else
		{
			if (_logger != null)
				_logger.info("could not find file " + f.getAbsolutePath());
			// throw new NullPointerException();
		}
		return toQuotedString(result);

	}

	private String toQuotedString(List<String> strings)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<strings.size(); i++)
		{
			String x = strings.get(i);
			if (x.contains(" ") && !(x.contains("\"") || x.contains("'")))
			{
				sb.append("\"");
				sb.append(x);
				sb.append("\"");
			}
			else
				sb.append(x);
			if (i != strings.size())
				sb.append(" ");
		}
		return sb.toString();
	}

	public String osCommand(String cmd)
	{
		StringBuffer result = new StringBuffer();
		try
		{
			Process p = Runtime.getRuntime().exec(cmd);
			InputStream in = p.getInputStream();
			int x;
			while ((x = in.read()) != -1)
				result.append((char) x);
		}
		catch (Exception ex)
		{
			if (_logger != null)
				_logger.warning("Error executing \"" + cmd + "\": " + ex);
		}
		return result.toString();
	}

	public String osCommand(String cmd, long timeout)
	{
		Process p = null;
		try
		{
			p = Runtime.getRuntime().exec(cmd);
			final Process fp = p;
			FutureTask<String> future = new FutureTask(new Callable()
			{

				public String call() throws Exception
				{
					StringBuffer result = new StringBuffer();
					InputStream in = fp.getInputStream();
					int x;
					while ((x = in.read()) != -1)
						result.append((char) x);

					return result.toString();
				}
			});
			executor.execute(future);
			String result = future.get(timeout, TimeUnit.MILLISECONDS);
			return result;

		}
		catch (Exception e)
		{
			if (_logger != null)
				_logger.warning("Error executing \"" + cmd + "\": " + e);
			if (p != null)
				p.destroy();
		}
		return null;
	}

	public static void main(String[] args)
	{
		System.out.println(new PosixUtils().osCommand("cmd /C dir", 500));
	}

}
