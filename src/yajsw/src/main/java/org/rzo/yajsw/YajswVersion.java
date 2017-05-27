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
package org.rzo.yajsw;

public class YajswVersion
{
	public static final String YAJSW_VERSION = "yajsw-stable-12.09";
	public static final String OS_VERSION = System.getProperty("os.name") + "/"
			+ System.getProperty("os.version") + "/"
			+ System.getProperty("os.arch");
	public static final String JAVA_VERSION = System.getProperty("java.vendor")
			+ "/"
			+ System.getProperty("java.version")
			+ "/"
			+ System.getProperty("java.home")
			+ "/"
			+ (System.getProperty("sun.arch.data.model") != null ? System
					.getProperty("sun.arch.data.model") : "unknown");
}
