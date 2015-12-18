package org.rzo.yajsw.util;

public interface MyKeyStoreInterface
{
	public void init() throws Exception;
	public void start() throws Exception;
	public void put(String key, char[] value) throws Exception;
	public char[] get(String key) throws Exception;
	public String getFile();

}
