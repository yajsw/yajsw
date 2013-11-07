package org.rzo.yajsw.boot;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.rzo.yajsw.boot.WrapperLoader;

public class RuntimeJavaMainBooter
{
	public static void main(String[] args)
	{
		URLClassLoader cl = WrapperLoader.getWrapperClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		try
		{
			Class cls = Class.forName("org.rzo.yajsw.app.RuntimeJavaMain", true, cl);
			Method mainMethod = cls.getDeclaredMethod("main", new Class[]
			{ String[].class });
			mainMethod.invoke(null, new Object[]
			{ args });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
