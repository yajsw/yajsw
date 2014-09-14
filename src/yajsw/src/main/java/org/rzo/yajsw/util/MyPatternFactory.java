package org.rzo.yajsw.util;

public class MyPatternFactory
{
	public static MyPatternInterface createPattern(String regex)
	{
		MyPatternInterface result = null;
		try
		{
			Class c = MyPatternFactory.class.getClassLoader().loadClass("org.rzo.yajsw.util.MyPattern");
			result = (MyPatternInterface) c.newInstance();
			result.setRegEx(regex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
