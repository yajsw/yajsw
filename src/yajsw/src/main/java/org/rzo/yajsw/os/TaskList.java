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

package org.rzo.yajsw.os;

import java.util.Collection;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface TaskList.
 */
public interface TaskList
{

	/**
	 * Adds the listner.
	 * 
	 * @param listner
	 *            the listner
	 */
	public void addListner(TaskListListner listner);

	/**
	 * Removes the listner.
	 * 
	 * @param listner
	 *            the listner
	 */
	public void removeListner(TaskListListner listner);

	/**
	 * Task list.
	 * 
	 * @return the map
	 */
	public Map taskList();

	/**
	 * The Interface TaskListListner.
	 */
	public interface TaskListListner
	{

		/**
		 * Changed.
		 * 
		 * @param event
		 *            the event
		 */
		public void changed(TaskListEvent event);
	}

	/**
	 * The Interface TaskListEvent.
	 */
	public interface TaskListEvent
	{

		/**
		 * Gets the new tasks.
		 * 
		 * @return the new tasks
		 */
		public Collection getNewTasks();

		/**
		 * Gets the removed tasks.
		 * 
		 * @return the removed tasks
		 */
		public Collection getRemovedTasks();

		/**
		 * Gets the current tasks.
		 * 
		 * @return the current tasks
		 */
		public Collection getCurrentTasks();
	}

}
