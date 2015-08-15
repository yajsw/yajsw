package org.rzo.netty.ahessian.crypto;

import java.security.SecureRandom;
import java.util.Arrays;

public class ClientCryptoData
{
	 StreamCipher _encodeCipher;
	 StreamCipher _decodeCipher;
	 byte[] _encodedPublicKey;
	 int _bytesRead;
	 SecureRandom _secureRandom = new SecureRandom();
	 byte[] _password = new byte[CryptoConstants.PASSWORD_SIZE];
	
	public ClientCryptoData()
	{
		Arrays.fill(_password, (byte)0);
	}


}
