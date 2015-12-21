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

import java.util.HashMap;
import java.util.Map;

public class CaseInsensitiveMap extends HashMap<String, String>
{

	public CaseInsensitiveMap(Map<String, String> map)
	{
		super();
		for (Map.Entry<String, String> entry : map.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String put(String key, String value)
	{
		return super.put(key.toLowerCase(), value);
	}

	@Override
	public String get(Object key)
	{
		return super.get(((String) key).toLowerCase());
	}
}
