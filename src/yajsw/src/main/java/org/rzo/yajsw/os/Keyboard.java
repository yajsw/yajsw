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

// TODO: Auto-generated Javadoc
/**
 * The Interface Keyboard.
 */
public interface Keyboard
{

	/** The Constant MOD_ALT. */
	static final int MOD_ALT = 1;

	/** The Constant MOD_CONTROL. */
	static final int MOD_CONTROL = 2;

	/** The Constant MOD_SHIFT. */
	static final int MOD_SHIFT = 4;

	/** The Constant MOD_WIN. */
	static final int MOD_WIN = 8;

	/**
	 * The Interface HotKeyListner.
	 */
	public interface HotKeyListner
	{

		/**
		 * Key pressed.
		 */
		public void keyPressed();
	}

	/**
	 * Register hotkey.
	 * 
	 * @param listner
	 *            the listner
	 * @param mod
	 *            the mod
	 * @param key
	 *            the key
	 */
	public void registerHotkey(HotKeyListner listner, int mod, int key);

	/**
	 * Unregister hot key.
	 * 
	 * @param listner
	 *            the listner
	 */
	public void unregisterHotKey(HotKeyListner listner);

}
