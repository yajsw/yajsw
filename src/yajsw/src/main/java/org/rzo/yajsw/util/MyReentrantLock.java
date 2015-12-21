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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MyReentrantLock extends ReentrantLock
{

	final static long timeout = 100; // millis

	@Override
	synchronized public void lock()
	{
		// To avoid a hang that seems to be caused by a lost-wakeup
		// we repeatedly use tryAcquire in a loop so that we can
		// poll the lock state

		boolean locked = false;
		boolean interrupted = false;

		while (!locked)
		{
			try
			{
				locked = tryLock(timeout, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException ex)
			{
				interrupted = true;
			}
		}

		if (interrupted)
		{
			// re-assert interrupt state that occurred while we
			// were acquiring the lock
			Thread.currentThread().interrupt();
		}
	}

}
