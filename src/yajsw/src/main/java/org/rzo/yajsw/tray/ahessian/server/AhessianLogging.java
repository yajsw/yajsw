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
package org.rzo.yajsw.tray.ahessian.server;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.logging.Logger;

public class AhessianLogging
{
	public static void setAhessianLogger(final Logger log)
	{
		InternalLoggerFactory.setDefaultFactory(new InternalLoggerFactory()
		{

			@Override
			public InternalLogger newInstance(String name)
			{
				return (InternalLogger) new JdkLogger(log, "ahessian-jmx");
			}
		});
	}

}
