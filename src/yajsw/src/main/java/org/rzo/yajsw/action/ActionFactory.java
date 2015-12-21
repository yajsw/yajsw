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

import java.io.PrintStream;

import org.rzo.yajsw.Constants;
import org.rzo.yajsw.controller.Message;

public class ActionFactory
{
	public static Action getAction(Message msg)
	{
		String cls = null;
		if (msg.getCode() == Constants.WRAPPER_MSG_THREAD_DUMP)
		{
			String version = System.getProperty("java.specification.version");
			if (version.startsWith("1.6"))
				cls = "org.rzo.yajsw.action.ThreadDumpImpl6";
			else
				cls = "org.rzo.yajsw.action.ThreadDumpImpl5";
		}
		if (cls != null)
			try
			{
				Class cl = ActionFactory.class.getClassLoader().loadClass(cls);
				return (Action) cl.newInstance();
			}
			catch (Throwable ex)
			{
				ex.printStackTrace();
			}
		return new Action()
		{

			public void execute(Message msg, Channel session, PrintStream out,
					Object data)
			{
				System.out.println("Error No Action for " + msg);
			}

		};

	}
}
