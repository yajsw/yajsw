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
package org.rzo.netty.ahessian.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;

public class Base64AuthToken extends SimpleAuthToken
{

	private String _user;

	public Base64AuthToken(String user, String password) throws Exception
	{
		_user = user;
		String data = user + ":" + password;
		ByteBuf digest = Base64.encode(Unpooled.wrappedBuffer(data
				.getBytes("UTF-8")));
		byte[] digestBytes = new byte[digest.readableBytes()];
		digest.readBytes(digestBytes);
		super.setPassword(new String(digestBytes));
	}

	public String getUser()
	{
		return _user;
	}

}
