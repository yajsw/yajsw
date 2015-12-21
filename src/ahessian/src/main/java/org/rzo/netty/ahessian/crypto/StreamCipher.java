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

public interface StreamCipher
{
	public void engineInitEncrypt(byte[] key, byte[] iv) throws CryptoException;

	public void engineInitDecrypt(byte[] key, byte[] iv) throws CryptoException;

	public void crypt(byte[] in, int inOffset, int length, byte[] out,
			int outOffset) throws CryptoException;

	public byte[] crypt(byte[] data, int position, int length)
			throws CryptoException;

}
