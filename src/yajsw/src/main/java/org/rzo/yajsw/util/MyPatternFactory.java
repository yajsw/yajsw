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

public class MyPatternFactory
{
	public static MyPatternInterface createPattern(String regex)
	{
		MyPatternInterface result = null;
		try
		{
			Class c = MyPatternFactory.class.getClassLoader().loadClass(
					"org.rzo.yajsw.util.MyPattern");
			result = (MyPatternInterface) c.newInstance();
			result.setRegEx(regex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
