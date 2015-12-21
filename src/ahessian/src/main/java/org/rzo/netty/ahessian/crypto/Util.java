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
package org.rzo.netty.ahessian.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Util implements CryptoConstants
{
	static ByteBuf code(StreamCipher cipher, ByteBuf e, boolean decode)
			throws Exception
	{
		try
		{
			ByteBuf b = e;
			byte[] encodedData = cipher.crypt(b.array(), b.readerIndex(),
					b.readableBytes());
			return Unpooled.wrappedBuffer(encodedData);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}

	}

}
