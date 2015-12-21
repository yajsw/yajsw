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
package org.rzo.yajsw.groovy;

import java.util.HashMap;
import java.util.Iterator;

public class XHashMap extends HashMap
{
	public Object put(Object key, Object value)
	{
		// System.out.println("put "+key+" "+value);
		XHashMap v = (XHashMap) super.get(key);
		if (v == null)
			v = new XHashMap();
		v.setValue(value);
		return super.put(key, v);
	}

	void setValue(Object value)
	{
		super.put("value", value);
	}

	Object getValue()
	{
		return super.get("value");
	}

	public Object get(Object key)
	{
		Object value = super.get(key);
		// System.out.println("get "+key+" "+value);
		if (value == null)
		{
			value = new XHashMap();
			super.put(key, value);
		}
		return value;
	}

	public HashMap toMap()
	{
		HashMap result = new HashMap();
		for (Iterator it = keySet().iterator(); it.hasNext();)
			toMap("wrapper", (String) it.next(), result, this);
		return result;
	}

	private void toMap(String keyPrefix, String key, HashMap result,
			XHashMap map)
	{
		if ("value".equals(key))
			result.put(keyPrefix, map.getValue());
		else
		{
			XHashMap v = (XHashMap) map.get(key);
			for (Iterator it = v.keySet().iterator(); it.hasNext();)
				toMap(keyPrefix + "." + key, (String) it.next(), result, v);
		}
	}

}
