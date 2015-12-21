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
package org.rzo.yajsw.controller.jvm;

import java.util.logging.Logger;

import org.rzo.yajsw.controller.AbstractController.ControllerListener;

public interface Controller
{

	void setDebug(int debug);

	void setLogger(Logger wrapperLogger);

	boolean start();

	void stop(int state, String reason);

	void addListener(int stateStopped, ControllerListener listenerStopped);

	void reset();

	void processStarted();

	void processFailed();

	void beginWaitForStartup();

	void setDebugComm(boolean debugComm);

}
