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

public interface CryptoConstants
{
	public static String ASYM_KEY_TYPE = "RSA";
	public static String ASYM_CIPHER_TYPE = "ECB/NOPADDING";
	public static String SYM_KEY_TYPE = "RC4";// "Salsa20";//"RC4";//
	// if lower than 512 java throws an exception
	// size in bits
	public static int ASYM_KEY_SIZE = 512;
	// size in bytes
	public static int SYM_KEY_SIZE = 16;
	// only required for salsa20, must be 8 byte
	// size in bytes
	public static int SYM_IV_SIZE = 8;

	// size of password in bytes
	// password is used to avoid MITM attacks
	public static int PASSWORD_SIZE = 15;

}
