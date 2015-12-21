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
package org.rzo.yajsw.wrapper;

import org.rzo.yajsw.boot.WrapperLoader;

public class WrappedRuntimeJavaProcess extends WrappedJavaProcess
{
	public void init()
	{
		super.init();
		_localConfiguration.setProperty("wrapper.java.app.mainclass",
				"org.rzo.yajsw.boot.RuntimeJavaMainBooter");
		_localConfiguration.setProperty("wrapper.java.classpath.x1",
				WrapperLoader.getWrapperJar());
		_localConfiguration.setProperty("wrapper.java.classpath.x2",
				WrapperLoader.getWrapperAppJar());
		if (_debug > 0)
		{
			_localConfiguration.setProperty("wrapper.java.additional.x1",
					"-Dwrapper.debug=true");
			_localConfiguration.setProperty("wrapper.java.additional.x11",
					"-Dwrapper.debug.level=" + _debug);
		}
		if (_config.getBoolean("wrapper.runtime.java.default.shutdown", false))
			_localConfiguration.setProperty("wrapper.java.additional.x2",
					"-Dwrapper.runtime.java.default.shutdown=true");
	}

}
