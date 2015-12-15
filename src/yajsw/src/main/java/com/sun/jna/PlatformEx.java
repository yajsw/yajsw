package com.sun.jna;

public class PlatformEx
{
	private static boolean		winVista	= false;
	private static boolean		macYosemite	= false;

	static
	{
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("windows"))
		{
			winVista = osName.contains("vista") || osName.contains(" 7") || osName.contains("2008") || osName.contains("2012") || osName.contains(" 8");
		}
	}

	static
	{
		if (Platform.isMac())
		try {
			String[] versionStr = System.getProperty("os.version").split("\\.");
			int maj = Integer.parseInt(versionStr[0]);
			int min = Integer.parseInt(versionStr[1]);
			macYosemite = (maj == 10 && min >= 10) || maj > 10;			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static boolean isWinVista()
	{
		return winVista;
	}
	
	public static boolean isMacYosemite()
	{
		return macYosemite;
	}

}
