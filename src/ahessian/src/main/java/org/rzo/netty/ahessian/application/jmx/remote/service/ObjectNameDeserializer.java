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
package org.rzo.netty.ahessian.application.jmx.remote.service;

import java.io.IOException;

import javax.management.ObjectName;

import com.caucho.hessian4.io.AbstractDeserializer;
import com.caucho.hessian4.io.AbstractHessianInput;

public class ObjectNameDeserializer extends AbstractDeserializer
{

	public Object readObject(AbstractHessianInput in, Object[] fields)
			throws IOException
	{
		String on = in.readString();
		try
		{
			Object result = new ObjectName(on);
			in.addRef(result);
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
