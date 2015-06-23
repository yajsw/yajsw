package org.rzo.yajsw.os.posix;

import java.util.ArrayList;
import java.util.List;

import org.rzo.yajsw.os.posix.PosixProcess.CLibrary;

public class PosixSpawnMain
{
	public static void main(String[] args)
	{
		// detach from parent
		CLibrary.INSTANCE.umask(0);
		CLibrary.INSTANCE.setsid();

		// set priority
		int nice = getNice();
		if (nice != 0)
		if (CLibrary.INSTANCE.nice(nice) == -1)
			System.out.println("could not set priority ");
		// set umask
		int umask = getUmask();
		if (umask != 0)
		if (CLibrary.INSTANCE.umask(umask) != 0)
			System.out.println("could not set umask ");
		
		// set user
		if (getUser() != null)
			try
		{
			new PosixProcess().switchUser(getUser(), getPassword());
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
		}
		
		// set working dir
		if (getWorkingdir() != null)
			if (CLibrary.INSTANCE.chdir(getWorkingdir()) != 0)
				System.out.println("could not set working dir");


		
		// close streams ?
	//	if (!isPipeStreams())
		{
			/*
			try
			{
				System.in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			*/
			
			//System.out.close();
			//System.err.close();
		}
		
		String[] env = null;//getEnv();

		// start the subprocess
		int ret = -1;
		try
		{
			if (env == null)
				CLibrary.INSTANCE.execvp(args[0], args);
			else
				CLibrary.INSTANCE.execvpe(args[0], args, env);
			System.out.println("ret "+ret);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

	private static boolean isPipeStreams()
	{
		return System.getProperty("wrapperx.pipeStreams") != null;
	}

	private static String getPassword()
	{
		return System.getProperty("wrapperx.password");
	}

	private static String getUser()
	{
		return System.getProperty("wrapperx.user");
	}

	private static String getWorkingdir()
	{
		return System.getProperty("wrapperx.workingdir");
	}

	private static int getNice()
	{
		String x = System.getProperty("wrapperx.nice", "0");
		return Integer.parseInt(x);
	}

	private static int getUmask()
	{
		String u = System.getProperty("wrapperx.umask", "0");
		return Integer.parseInt(u);
	}

	private static String[] getEnv()
	{
		List<String> result = new ArrayList<String>();
		for (String key : System.getenv().keySet())
		{
			result.add(key+"="+System.getenv(key));
		}
		if (result.isEmpty())
			return null;
		String[] arr = new String[result.size()];
		int i = 0;
		for (String x : result)
		{
			arr[i] = x;
			System.out.println(x);
			i++;
		}
		return arr;
	}

}
