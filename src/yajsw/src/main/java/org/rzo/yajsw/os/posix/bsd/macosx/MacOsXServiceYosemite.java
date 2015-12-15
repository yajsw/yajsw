package org.rzo.yajsw.os.posix.bsd.macosx;

public class MacOsXServiceYosemite extends MacOsXService
{
	@Override
	protected String getDefaultLaunchDir()
	{
		return "/Library/LaunchDaemons";
	}
	
	@Override
	protected String getPlistPrefix()
	{
		return "org.rzo.yajsw.";
	}
	
	@Override
	protected void preload()
	{
		System.out.println(_utils.osCommand("chmod 600 " + _plistFile, 5000));
		System.out.println(_utils.osCommand("chown root " + _plistFile, 5000));
	}





}
