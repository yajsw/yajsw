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
package org.rzo.netty.ahessian.session;

import java.util.Collection;

public interface ServiceSession
{
	public String getId();

	public void addClosedListener(Runnable listener);

	public void addInvalidatedListener(Runnable listener);

	public Object getAttribute(String name);

	public Collection<String> getAttributeNames();

	public void removeAttribute(String name);

	public void setAttribute(String name, Object value);

	public long getCreationTime();

	public long getLastConnectedTime();

	public boolean isValid();

	public boolean isClosed();

	public boolean isNew();

	public long getMessageCount();

}
