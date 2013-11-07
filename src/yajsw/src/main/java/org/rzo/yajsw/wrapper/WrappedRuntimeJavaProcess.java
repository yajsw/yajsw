package org.rzo.yajsw.wrapper;

import java.util.Iterator;

import org.rzo.yajsw.app.RuntimeJavaMain;
import org.rzo.yajsw.boot.WrapperLoader;

public class WrappedRuntimeJavaProcess extends WrappedJavaProcess
{
	public void init()
	{
		super.init();
		_config.getSystemConfiguration().setProperty("wrapper.java.app.mainclass", "org.rzo.yajsw.boot.RuntimeJavaMainBooter");
		_config.getSystemConfiguration().setProperty("wrapper.java.classpath.x1", WrapperLoader.getWrapperJar());
		_config.getSystemConfiguration().setProperty("wrapper.java.classpath.x2", WrapperLoader.getWrapperAppJar());
		//_config.getSystemConfiguration().setProperty("wrapper.app.parameter.0", _config.getString("wrapper.image"));
		/*
		Iterator<String> keys = _config.getKeys("wrapper.app.parameter");
		while (keys.hasNext())
		{
			String key = keys.next();
			_config.getSystemConfiguration().setProperty(key, _config.getString(key));			
		}
		*/
	}

}
