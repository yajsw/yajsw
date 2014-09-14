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
		String data = user+":"+password;
		ByteBuf digest = Base64.encode(Unpooled.wrappedBuffer(data.getBytes("UTF-8")));
		byte[] digestBytes = new byte[digest.readableBytes()];
		digest.readBytes(digestBytes);
		super.setPassword(new String(digestBytes));
	}
	
	public String getUser()
	{
		return _user;
	}

}
