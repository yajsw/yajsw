package org.rzo.yajsw.wrapper;

import java.util.Iterator;

import org.rzo.yajsw.app.RuntimeJavaMain;
import org.rzo.yajsw.boot.WrapperLoader;

public class WrappedRuntimeJavaProcess extends WrappedJavaProcess
{
	public void init()
	{
		super.init();
		_localConfiguration.setProperty("wrapper.java.app.mainclass", "org.rzo.yajsw.boot.RuntimeJavaMainBooter");
		_localConfiguration.setProperty("wrapper.java.classpath.x1", WrapperLoader.getWrapperJar());
		_localConfiguration.setProperty("wrapper.java.classpath.x2", WrapperLoader.getWrapperAppJar());
		if (_debug)
			_localConfiguration.setProperty("wrapper.java.additional.x1", "-Dwrapper.debug=true");
		if (_config.getBoolean("wrapper.runtime.java.default.shutdown", false))
			_localConfiguration.setProperty("wrapper.java.additional.x2", "-Dwrapper.runtime.java.default.shutdown=true");			
	}

}
