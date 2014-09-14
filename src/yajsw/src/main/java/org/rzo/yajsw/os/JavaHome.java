package org.rzo.yajsw.os;

import io.netty.util.internal.logging.InternalLogger;

public interface JavaHome
{
	String findJava(String wrapperJava, String customProcessName);
	void setLogger(InternalLogger logger);

}
