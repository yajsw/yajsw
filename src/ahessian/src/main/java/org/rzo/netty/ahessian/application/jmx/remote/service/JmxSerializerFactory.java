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

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.rzo.netty.ahessian.rpc.message.MappingSerializerFactory;

import com.caucho.hessian4.io.ObjectNameDeserializer;
import com.caucho.hessian4.io.StringValueSerializer;

public class JmxSerializerFactory extends MappingSerializerFactory
{
	static Map<String, String> serializers = new HashMap<String, String>();
	static Map<String, String> deserializers = new HashMap<String, String>();

	static
	{
		serializers.put(ObjectName.class.getName(),
				StringValueSerializer.class.getName());
		deserializers.put(ObjectName.class.getName(),
				ObjectNameDeserializer.class.getName());
	}

	public JmxSerializerFactory()
	{
		super(serializers, deserializers);
	}

}
