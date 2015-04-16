package org.rzo.yajsw.wrapper;

import org.rzo.yajsw.boot.WrapperLoader;

public class WrappedRuntimeJavaProcess extends WrappedJavaProcess
{
	public void init()
	{
		super.init();
		_localConfiguration.setProperty("wrapper.java.app.mainclass", "org.rzo.yajsw.boot.RuntimeJavaMainBooter");
		_localConfiguration.setProperty("wrapper.java.classpath.x1", WrapperLoader.getWrapperJar());
		_localConfiguration.setProperty("wrapper.java.classpath.x2", WrapperLoader.getWrapperAppJar());
		if (_debug > 0)
		{
			_localConfiguration.setProperty("wrapper.java.additional.x1", "-Dwrapper.debug=true");
			_localConfiguration.setProperty("wrapper.java.additional.x11", "-Dwrapper.debug.level="+_debug);
		}
		if (_config.getBoolean("wrapper.runtime.java.default.shutdown", false))
			_localConfiguration.setProperty("wrapper.java.additional.x2", "-Dwrapper.runtime.java.default.shutdown=true");			
	}

}
