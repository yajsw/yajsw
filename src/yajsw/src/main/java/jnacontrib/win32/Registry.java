/*
 * Registry.java
 *
 * Created on 17. August 2007, 15:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jnacontrib.win32;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import jnacontrib.jna.Advapi32;
import jnacontrib.jna.WINERROR;

import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Methods for accessing the Windows Registry. Only String and DWORD values
 * supported at the moment.
 */
public class Registry
{
	
	public final static int	HKEY_CLASSES_ROOT	= 0x80000000;
	public final static int	HKEY_CURRENT_USER	= 0x80000001;
	public final static int	HKEY_LOCAL_MACHINE	= 0x80000002;
	public final static int	HKEY_USERS			= 0x80000003;

	public static enum REGISTRY_ROOT_KEY
	{
		CLASSES_ROOT, CURRENT_USER, LOCAL_MACHINE, USERS
	};

	private final static HashMap<REGISTRY_ROOT_KEY, HKEY>	rootKeyMap	= new HashMap<REGISTRY_ROOT_KEY, HKEY>();

	static
	{
		rootKeyMap.put(REGISTRY_ROOT_KEY.CLASSES_ROOT, new HKEY(HKEY_CLASSES_ROOT));
		rootKeyMap.put(REGISTRY_ROOT_KEY.CURRENT_USER, new HKEY(HKEY_CURRENT_USER));
		rootKeyMap.put(REGISTRY_ROOT_KEY.LOCAL_MACHINE, new HKEY(HKEY_LOCAL_MACHINE));
		rootKeyMap.put(REGISTRY_ROOT_KEY.USERS, new HKEY(HKEY_USERS));
	}

	/**
	 * Testing.
	 * 
	 * @param args
	 *            arguments
	 * @throws java.lang.Exception
	 *             on error
	 */
	public static void main(String[] args) throws Exception
	{
	}

	/**
	 * Gets one of the root keys.
	 * 
	 * @param key
	 *            key type
	 * @return root key
	 */
	private static HKEY getRegistryRootKey(REGISTRY_ROOT_KEY key)
	{
		Advapi32 advapi32;
		HKEYByReference pHandle;
		HKEY handle = null;

		advapi32 = Advapi32.INSTANCE;
		pHandle = new WinReg.HKEYByReference();

		if (advapi32.RegOpenKeyEx(rootKeyMap.get(key), null, 0, 0, pHandle) == WINERROR.ERROR_SUCCESS)
		{
			handle = pHandle.getValue();
		}
		return (handle);
	}

	/**
	 * Opens a key.
	 * 
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            name of the key
	 * @param access
	 *            access mode
	 * @return handle to the key or 0
	 */
	private static HKEY openKey(REGISTRY_ROOT_KEY rootKey, String subKeyName, int access)
	{
		Advapi32 advapi32;
		HKEYByReference pHandle;
		HKEY rootKeyHandle;

		advapi32 = Advapi32.INSTANCE;
		rootKeyHandle = getRegistryRootKey(rootKey);
		pHandle = new HKEYByReference();

		if (advapi32.RegOpenKeyEx(rootKeyHandle, subKeyName, 0, access, pHandle) == WINERROR.ERROR_SUCCESS)
		{
			return (pHandle.getValue());

		}
		else
		{
			return (null);
		}
	}

	/**
	 * Converts a Windows buffer to a Java String.
	 * 
	 * @param buf
	 *            buffer
	 * @throws java.io.UnsupportedEncodingException
	 *             on error
	 * @return String
	 */
	private static String convertBufferToString(byte[] buf) throws UnsupportedEncodingException
	{
		return (new String(buf, 0, buf.length - 2, "UTF-16LE"));
	}

	/**
	 * Converts a Windows buffer to an int.
	 * 
	 * @param buf
	 *            buffer
	 * @return int
	 */
	private static int convertBufferToInt(byte[] buf)
	{
		return (((int) (buf[0] & 0xff)) + (((int) (buf[1] & 0xff)) << 8) + (((int) (buf[2] & 0xff)) << 16) + (((int) (buf[3] & 0xff)) << 24));
	}

	/**
	 * Read a String value.
	 * 
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            key name
	 * @param name
	 *            value name
	 * @throws java.io.UnsupportedEncodingException
	 *             on error
	 * @return String or null
	 */
	public static String getStringValue(REGISTRY_ROOT_KEY rootKey, String subKeyName, String name) throws UnsupportedEncodingException
	{
		Advapi32 advapi32;
		IntByReference pType, lpcbData;
		byte[] lpData = new byte[1];
		HKEY handle = null;
		String ret = null;

		advapi32 = Advapi32.INSTANCE;
		pType = new IntByReference();
		lpcbData = new IntByReference();
		handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

		if (handle != null)
		{

			if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_MORE_DATA)
			{
				lpData = new byte[lpcbData.getValue()];

				if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_SUCCESS)
				{
					ret = convertBufferToString(lpData);
				}
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Read an int value.
	 * 
	 * 
	 * @return int or 0
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            key name
	 * @param name
	 *            value name
	 */
	public static int getIntValue(REGISTRY_ROOT_KEY rootKey, String subKeyName, String name)
	{
		Advapi32 advapi32;
		IntByReference pType, lpcbData;
		byte[] lpData = new byte[1];
		HKEY handle = null;
		int ret = 0;

		advapi32 = Advapi32.INSTANCE;
		pType = new IntByReference();
		lpcbData = new IntByReference();
		handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

		if (handle != null)
		{

			if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_MORE_DATA)
			{
				lpData = new byte[lpcbData.getValue()];

				if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_SUCCESS)
				{
					ret = convertBufferToInt(lpData);
				}
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Delete a value.
	 * 
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            key name
	 * @param name
	 *            value name
	 * @return true on success
	 */
	public static boolean deleteValue(REGISTRY_ROOT_KEY rootKey, String subKeyName, String name)
	{
		Advapi32 advapi32;
		HKEY handle;
		boolean ret = true;

		advapi32 = Advapi32.INSTANCE;

		handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

		if (handle != null)
		{
			if (advapi32.RegDeleteValue(handle, name) == WINERROR.ERROR_SUCCESS)
			{
				ret = true;
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Writes a String value.
	 * 
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            key name
	 * @param name
	 *            value name
	 * @param value
	 *            value
	 * @throws java.io.UnsupportedEncodingException
	 *             on error
	 * @return true on success
	 */
	public static boolean setStringValue(REGISTRY_ROOT_KEY rootKey, String subKeyName, String name, String value) throws UnsupportedEncodingException
	{
		Advapi32 advapi32;
		HKEY handle;
		byte[] data;
		boolean ret = false;

		data = Arrays.copyOf(value.getBytes("UTF-16LE"), value.length() * 2 + 2);
		advapi32 = Advapi32.INSTANCE;
		handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

		if (handle != null)
		{
			if (advapi32.RegSetValueEx(handle, name, 0, WinNT.REG_SZ, data, data.length) == WINERROR.ERROR_SUCCESS)
			{
				ret = true;
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Writes an int value.
	 * 
	 * 
	 * @return true on success
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            key name
	 * @param name
	 *            value name
	 * @param value
	 *            value
	 */
	public static boolean setIntValue(REGISTRY_ROOT_KEY rootKey, String subKeyName, String name, int value)
	{
		Advapi32 advapi32;
		HKEY handle;
		byte[] data;
		boolean ret = false;

		data = new byte[4];
		data[0] = (byte) (value & 0xff);
		data[1] = (byte) ((value >> 8) & 0xff);
		data[2] = (byte) ((value >> 16) & 0xff);
		data[3] = (byte) ((value >> 24) & 0xff);
		advapi32 = Advapi32.INSTANCE;
		handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

		if (handle != null)
		{

			if (advapi32.RegSetValueEx(handle, name, 0, WinNT.REG_DWORD, data, data.length) == WINERROR.ERROR_SUCCESS)
			{
				ret = true;
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Check for existence of a value.
	 * 
	 * @param rootKey
	 *            root key
	 * @param subKeyName
	 *            key name
	 * @param name
	 *            value name
	 * @return true if exists
	 */
	public static boolean valueExists(REGISTRY_ROOT_KEY rootKey, String subKeyName, String name)
	{
		Advapi32 advapi32;
		IntByReference pType, lpcbData;
		byte[] lpData = new byte[1];
		HKEY handle = null;
		boolean ret = false;

		advapi32 = Advapi32.INSTANCE;
		pType = new IntByReference();
		lpcbData = new IntByReference();
		handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

		if (handle != null)
		{

			if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) != WINERROR.ERROR_FILE_NOT_FOUND)
			{
				ret = true;

			}
			else
			{
				ret = false;
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Create a new key.
	 * 
	 * @param rootKey
	 *            root key
	 * @param parent
	 *            name of parent key
	 * @param name
	 *            key name
	 * @return true on success
	 */
	public static boolean createKey(REGISTRY_ROOT_KEY rootKey, String parent, String name)
	{
		Advapi32 advapi32;
		HKEYByReference hkResult;
		IntByReference dwDisposition;
		HKEY handle = null;
		boolean ret = false;

		advapi32 = Advapi32.INSTANCE;
		hkResult = new HKEYByReference();
		dwDisposition = new IntByReference();
		handle = openKey(rootKey, parent, WinNT.KEY_READ);

		if (handle != null)
		{

			if (advapi32.RegCreateKeyEx(handle, name, 0, null, WinNT.REG_OPTION_NON_VOLATILE, WinNT.KEY_READ, null, hkResult, dwDisposition) == WINERROR.ERROR_SUCCESS)
			{
				ret = true;
				advapi32.RegCloseKey(hkResult.getValue());

			}
			else
			{
				ret = false;
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Delete a key.
	 * 
	 * @param rootKey
	 *            root key
	 * @param parent
	 *            name of parent key
	 * @param name
	 *            key name
	 * @return true on success
	 */
	public static boolean deleteKey(REGISTRY_ROOT_KEY rootKey, String parent, String name)
	{
		Advapi32 advapi32;
		HKEY handle = null;
		boolean ret = false;

		advapi32 = Advapi32.INSTANCE;
		handle = openKey(rootKey, parent, WinNT.KEY_READ);

		if (handle != null)
		{

			if (advapi32.RegDeleteKey(handle, name) == WINERROR.ERROR_SUCCESS)
			{
				ret = true;

			}
			else
			{
				ret = false;
			}
			advapi32.RegCloseKey(handle);
		}
		return (ret);
	}

	/**
	 * Get all sub keys of a key.
	 * 
	 * @param rootKey
	 *            root key
	 * @param parent
	 *            key name
	 * @return array with all sub key names
	 */
	public static String[] getSubKeys(REGISTRY_ROOT_KEY rootKey, String parent)
	{
		Advapi32 advapi32;
		HKEY handle = null;
		int dwIndex;
		char[] lpName;
		IntByReference lpcName;
		WinBase.FILETIME lpftLastWriteTime;
		TreeSet<String> subKeys = new TreeSet<String>();

		advapi32 = Advapi32.INSTANCE;
		handle = openKey(rootKey, parent, WinNT.KEY_READ);
		lpName = new char[256];
		lpcName = new IntByReference(256);
		lpftLastWriteTime = new WinNT.FILETIME();

		if (handle != null)
		{
			dwIndex = 0;

			while (advapi32.RegEnumKeyEx(handle, dwIndex, lpName, lpcName, null, null, null, lpftLastWriteTime) == WINERROR.ERROR_SUCCESS)
			{
				subKeys.add(new String(lpName, 0, lpcName.getValue()));
				lpcName.setValue(256);
				dwIndex++;
			}
			advapi32.RegCloseKey(handle);
		}

		return (subKeys.toArray(new String[]
		{}));
	}

	/**
	 * Get all values under a key.
	 * 
	 * @param rootKey
	 *            root key
	 * @param key
	 *            jey name
	 * @throws java.io.UnsupportedEncodingException
	 *             on error
	 * @return TreeMap with name and value pairs
	 */
	public static TreeMap<String, Object> getValues(REGISTRY_ROOT_KEY rootKey, String key) throws UnsupportedEncodingException
	{
		Advapi32 advapi32;
		HKEY handle = null;
		int dwIndex, result = 0;
		char[] lpValueName;
		byte[] lpData;
		IntByReference lpcchValueName, lpType, lpcbData;
		String name;
		TreeMap<String, Object> values = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

		advapi32 = Advapi32.INSTANCE;
		handle = openKey(rootKey, key, WinNT.KEY_READ);
		lpValueName = new char[16384];
		lpcchValueName = new IntByReference(16384);
		lpType = new IntByReference();
		lpData = new byte[1];
		lpcbData = new IntByReference();

		if (handle != null)
		{
			dwIndex = 0;

			do
			{
				lpcbData.setValue(0);
				result = advapi32.RegEnumValue(handle, dwIndex, lpValueName, lpcchValueName, null, lpType, lpData, lpcbData);

				if (result == WINERROR.ERROR_MORE_DATA)
				{
					lpData = new byte[lpcbData.getValue()];
					lpcchValueName = new IntByReference(16384);
					result = advapi32.RegEnumValue(handle, dwIndex, lpValueName, lpcchValueName, null, lpType, lpData, lpcbData);

					if (result == WINERROR.ERROR_SUCCESS)
					{
						name = new String(lpValueName, 0, lpcchValueName.getValue());

						switch (lpType.getValue())
						{
						case WinNT.REG_SZ:
							values.put(name, convertBufferToString(lpData));
							break;
						case WinNT.REG_DWORD:
							values.put(name, convertBufferToInt(lpData));
							break;
						default:
							break;
						}
					}
				}
				dwIndex++;
			}
			while (result == WINERROR.ERROR_SUCCESS);

			advapi32.RegCloseKey(handle);
		}
		return (values);
	}
}
