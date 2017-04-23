/*******************************************************************************
 * Copyright  2015 rzorzorzo@users.sf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.rzo.yajsw.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.rzo.yajsw.wrapper.WrappedProcess;
import org.rzo.yajsw.wrapper.WrappedService;

import com.sun.jna.PlatformEx;

import sun.net.InetAddressCachePolicy;

public class Utils
{
	WrappedProcess _process;
	WrappedService _service;

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
	
	public static InetAddress getLoopbackAddress() throws Exception
	{
		if (Boolean.getBoolean("java.net.preferIPv6Stack")) {
			byte[] arg0 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
			return InetAddress.getByAddress("localhost", arg0);
		} else {
			byte[] arg0 = new byte[] { 127, 0, 0, 1 };
			 return InetAddress.getByAddress("localhost", arg0);
		}
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
				System.out
						.println("!! WARNING !! Windows JDK7 should set -Djava.net.preferIPv4Stack=true (see java bug 7179799 )");
		}
	}

	public static String getDOption(String key, String value)
	{
		if (value == null)
		{
			System.out.println("null d option: "+key);
			return null;
		}
		// value = value.replace("\"", "\\\"");
		// value = value.replace("\\", "\\\\");
		value = value.replaceAll("\"", "");

		// if (value != null && !value.contains(" "))
		return "-D" + key + "=" + value;
		// else
		// return "\"-D" + key.trim() + "=" + value.trim() + "\"";
	}

	public static int parseOctal(String txt)
	{
		int result = -1;
		if (txt != null)
			try
			{
				result = Integer.parseInt(txt, 8);
			}
			catch (Exception ex)
			{
				System.out.println(ex + " " + ex.getMessage());
			}
		return result;

	}

}
