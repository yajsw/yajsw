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

package org.rzo.yajsw.tray;

/**
 * taken from
 * http://groups.google.com/group/comp.lang.java.help/browse_thread/thread
 * /0db818517ca9de79/b0a55aa19f911204 thanks to Piotr Kobzda Formatter for Bytes
 */
public class ByteFormat
{
	/**
	 * The Enum StorageUnit.
	 */
	public enum StorageUnit
	{

		/** The BYTE. */
		BYTE("B", 1L),
		/** The KILOBYTE. */
		KILOBYTE("KB", 1L << 10),
		/** The MEGABYTE. */
		MEGABYTE("MB", 1L << 20),
		/** The GIGABYTE. */
		GIGABYTE("GB", 1L << 30),
		/** The TERABYTE. */
		TERABYTE("TB", 1L << 40),
		/** The PETABYTE. */
		PETABYTE("PB", 1L << 50),
		/** The EXABYTE. */
		EXABYTE("EB", 1L << 60);

		/** The Constant BASE. */
		public static final StorageUnit BASE = BYTE;

		private final String symbol;
		private final long divider; // divider of BASE unit

		/**
		 * Instantiates a new storage unit.
		 * 
		 * @param name
		 *            the name
		 * @param divider
		 *            the divider
		 */
		StorageUnit(String name, long divider)
		{
			this.symbol = name;
			this.divider = divider;
		}

		/**
		 * Of.
		 * 
		 * @param number
		 *            the number
		 * 
		 * @return the storage unit
		 */
		public static StorageUnit of(final long number)
		{
			final long n = number > 0 ? -number : number;
			if (n > -(1L << 10))
			{
				return BYTE;
			}
			else if (n > -(1L << 20))
			{
				return KILOBYTE;
			}
			else if (n > -(1L << 30))
			{
				return MEGABYTE;
			}
			else if (n > -(1L << 40))
			{
				return GIGABYTE;
			}
			else if (n > -(1L << 50))
			{
				return TERABYTE;
			}
			else if (n > -(1L << 60))
			{
				return PETABYTE;
			}
			else
			{ // n >= Long.MIN_VALUE
				return EXABYTE;
			}
		}
	}

	/**
	 * Format.
	 * 
	 * @param number
	 *            the number of bytes
	 * 
	 * @return the formatted string
	 */
	public String format(long number)
	{
		StorageUnit st = StorageUnit.of(number);
		return nf.format((double) number / st.divider) + " " + st.symbol;
	}

	private static java.text.NumberFormat nf = java.text.NumberFormat
			.getInstance();
	static
	{
		nf.setGroupingUsed(false);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);
	}

}
