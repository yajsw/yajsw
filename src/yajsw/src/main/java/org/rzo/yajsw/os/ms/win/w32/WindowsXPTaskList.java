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

package org.rzo.yajsw.os.ms.win.w32;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rzo.yajsw.os.TaskList;

// TODO: Auto-generated Javadoc
/**
 * The Class WindowsXPTaskList.
 */
public class WindowsXPTaskList implements TaskList
{

	/** The _listners. */
	List _listners = new ArrayList();

	/** The _maps. */
	Map[] _maps;

	/** The _maps lock. */
	Map _mapsLock = new HashMap();

	/** The _instance. */
	static WindowsXPTaskList _instance;

	/** The _worker. */
	Worker _worker = new Worker();

	/**
	 * Instance.
	 * 
	 * @return the windows xp task list
	 */
	public static WindowsXPTaskList instance()
	{
		if (_instance == null)
		{
			_instance = new WindowsXPTaskList();
		}
		return _instance;
	}

	/**
	 * Instantiates a new windows xp task list.
	 */
	private WindowsXPTaskList()
	{
		synchronized (_mapsLock)
		{
			_maps = WindowsXPProcess.getProcessMaps(0);
		}

		new Thread(_worker).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rzo.yajsw.os.TaskList#addListner(org.rzo.yajsw.os.TaskList.
	 * TaskListListner)
	 */
	public synchronized void addListner(TaskListListner listner)
	{
		_listners.add(listner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rzo.yajsw.os.TaskList#removeListner(org.rzo.yajsw.os.TaskList.
	 * TaskListListner)
	 */
	public synchronized void removeListner(TaskListListner listner)
	{
		_listners.remove(listner);
	}

	/**
	 * The Class TaskListEvent.
	 */
	public class TaskListEvent implements
			org.rzo.yajsw.os.TaskList.TaskListEvent
	{

		/** The _new tasks. */
		Collection _newTasks;

		/** The _removed tasks. */
		Collection _removedTasks;

		/** The _current tasks. */
		Collection _currentTasks;

		/**
		 * Instantiates a new task list event.
		 * 
		 * @param newTasks
		 *            the new tasks
		 * @param removedTasks
		 *            the removed tasks
		 * @param currentTasks
		 *            the current tasks
		 */
		TaskListEvent(Collection newTasks, Collection removedTasks,
				Collection currentTasks)
		{
			_newTasks = newTasks;
			_removedTasks = removedTasks;
			_currentTasks = currentTasks;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.rzo.yajsw.os.TaskList.TaskListEvent#getNewTasks()
		 */
		public Collection getNewTasks()
		{
			return _newTasks;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.rzo.yajsw.os.TaskList.TaskListEvent#getRemovedTasks()
		 */
		public Collection getRemovedTasks()
		{
			return _removedTasks;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.rzo.yajsw.os.TaskList.TaskListEvent#getCurrentTasks()
		 */
		public Collection getCurrentTasks()
		{
			return _currentTasks;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rzo.yajsw.os.TaskList#taskList()
	 */
	public Map taskList()
	{
		synchronized (_mapsLock)
		{
			if (_maps == null)
				return null;
			else
				return new HashMap(_maps[0]);
		}
	}

	/**
	 * The Class Worker.
	 */
	class Worker implements Runnable
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(250);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				synchronized (_mapsLock)
				{
					synchronized (_listners)
					{
						Map[] maps2 = WindowsXPProcess.getProcessMaps(0);
						Set newSet = new HashSet(maps2[0].keySet());
						newSet.removeAll(_maps[0].keySet());
						Set deleteSet = new HashSet(_maps[0].keySet());
						deleteSet.removeAll(maps2[0].keySet());
						_maps = maps2;
						if ((newSet.size() > 0 || deleteSet.size() > 0)
								&& _listners.size() > 0)
						{
							TaskListEvent event = new TaskListEvent(newSet,
									deleteSet, _maps[0].keySet());
							for (Iterator it = _listners.iterator(); it
									.hasNext();)
								try
								{
									((TaskListListner) it.next())
											.changed(event);
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
								}
						}
					}
				}

			}

		}
	}

}
