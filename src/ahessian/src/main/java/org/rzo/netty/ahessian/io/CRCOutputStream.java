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
package org.rzo.netty.ahessian.io;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class CRCOutputStream extends OutputStreamBuffer
{
	public CRCOutputStream(ChannelHandlerContext ctx)
	{
		super(ctx);
	}

	byte _crc = 0;

	public void resetCRC()
	{
		_crc = 0;
	}

	public byte getCRC()
	{
		return _crc;
	}

	@Override
	public void write(int b) throws IOException
	{
		super.write(b);
		_crc ^= (byte) b;
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException
	{
		super.write(b, off, len);
		for (int i = off; i < off + len; i++)
		{
			_crc ^= (byte) b[i];
		}
	}

}
