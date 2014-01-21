package org.rzo.yajsw.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedService;

import com.sun.jna.PlatformEx;

public class Utils
{
	WrappedProcess	_process;
	WrappedService	_service;

	public Utils(WrappedProcess process)
	{
		_process = process;
	}

	public Utils(WrappedService service)
	{
		_service = service;
	}

	public String inquireCLI(String message) throws IOException
	{
		System.out.print(message + ":");
		return new BufferedReader(new InputStreamReader(System.in)).readLine();
	}

	public String inquireTrayIcon(String message) throws InterruptedException
	{
		String result = null;
		if (_process == null)
		{
			System.out.println("ERROR in inquireTrayIcon: process == null");
			return null;
		}
		while (result == null)
		{
			result = _process.getTrayIcon().inquire(message);
			if (result == null)
				Thread.sleep(2000);
		}
		return result;
	}
	
	public static void verifyIPv4IsPreferred(Logger log)
	{
		boolean ipv4Preferred = Boolean.getBoolean("java.net.preferIPv4Stack");
		boolean isJDK7 = System.getProperty("java.version").startsWith("1.7");
		boolean isWindows = PlatformEx.isWinVista();
		if (isWindows && isJDK7 && !ipv4Preferred)
		{
			if (log != null)
			log.warning("!! WARNING !! Windows JDK7 should set -Djava.net.preferIPv4Stack=true (see java bug 7179799 )");
			else
				System.out.println("!! WARNING !! Windows JDK7 should set -Djava.net.preferIPv4Stack=true (see java bug 7179799 )");
		}
	}
	
	public static String getDOption(String key, String value)
	{
		//value = value.replace("\"", "\\\"");
		//value = value.replace("\\", "\\\\");
		value = value.replaceAll("\"", "");

		if (value != null && !value.contains(" "))
			return "-D" + key + "=" + value;
		else
			return "-D" + key + "=\"" + value + "\"";
	}




}
