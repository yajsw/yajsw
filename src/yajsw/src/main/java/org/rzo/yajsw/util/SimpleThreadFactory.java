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

import java.util.concurrent.ThreadFactory;

public class SimpleThreadFactory implements ThreadFactory
{
	String _prefix;
	int _count = 0;
	static ThreadGroup group = new ThreadGroup("yajsw");
	int _priority = Thread.NORM_PRIORITY;

	public SimpleThreadFactory(String prefix)
	{
		_prefix = "yajsw." + prefix + "-";
	}

	public SimpleThreadFactory(String prefix, int priority)
	{
		_prefix = "yajsw." + prefix + "-";
		_priority = priority;
	}

	public Thread newThread(Runnable r)
	{
		Thread t = new Thread(group, r, _prefix + _count++);
		t.setPriority(_priority);
		return t;
	}
}
