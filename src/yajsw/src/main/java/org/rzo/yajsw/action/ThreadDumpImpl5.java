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
package org.rzo.yajsw.action;

import io.netty.channel.Channel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.rzo.yajsw.controller.Message;

public class ThreadDumpImpl5 implements Action
{
	public void execute(Message msg, Channel session, PrintStream out,
			Object data) throws IOException
	{
		Map allThreads = Thread.getAllStackTraces();
		Iterator iterator = allThreads.keySet().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		Set<Long> ids = new HashSet<Long>();
		if (data != null)
			for (long id : (long[]) data)
			{
				ids.add(id);
			}
		while (iterator.hasNext())
		{
			Thread key = (Thread) iterator.next();
			if (data != null && !ids.contains(key.getId()))
				continue;
			StackTraceElement[] trace = (StackTraceElement[]) allThreads
					.get(key);
			stringBuffer.append(key + "\r\n");
			for (int i = 0; i < trace.length; i++)
			{
				stringBuffer.append("  " + trace[i] + "\r\n");
			}
			stringBuffer.append("\r\n");
		}
		out.println(stringBuffer.toString());
		out.flush();
	}

	public static void main(String[] args) throws IOException
	{
		Action a = (Action) new ThreadDumpImpl5();
		a.execute(null, null, System.out, null);
	}

}
