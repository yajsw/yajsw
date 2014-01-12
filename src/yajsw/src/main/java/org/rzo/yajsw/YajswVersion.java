package org.rzo.yajsw;

public class YajswVersion
{
	public static final String YAJSW_VERSION = "yajsw-stable-11.11";
	public static final String OS_VERSION = System.getProperty("os.name")+"/"+System.getProperty("os.version")+"/"+System.getProperty("os.arch");
	public static final String JAVA_VERSION = System.getProperty("java.vendor")+"/"+System.getProperty("java.version")+"/"+System.getProperty("java.home")+"/"+(System.getProperty("sun.arch.data.model") != null ? System.getProperty("sun.arch.data.model"):"unknown");
}
