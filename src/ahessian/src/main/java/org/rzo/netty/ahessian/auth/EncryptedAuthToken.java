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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An Encrypted Authentication Token. The password is transmitted encrypted.
 */
public class EncryptedAuthToken extends SimpleAuthToken
{

	/** The _algorithm. */
	MessageDigest _algorithm = null;

	/**
	 * Sets the algorithm.
	 * 
	 * @param algorithm
	 *            the encryption algorithm.
	 * @see java.security.MessageDigest
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public void setAlgorithm(String algorithm) throws NoSuchAlgorithmException
	{
		_algorithm = MessageDigest.getInstance(algorithm);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rzo.netty.ahessian.auth.SimpleAuthToken#setPassword(java.lang.String)
	 */
	public void setPassword(String password)
	{
		_algorithm.reset();
		_algorithm.update(password.getBytes());
		_password = ensureLength(_algorithm.digest());
		_receivedBytes = new byte[_password.length];

	}

}
