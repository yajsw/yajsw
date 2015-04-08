/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package org.rzo.yajsw.timer;

import org.rzo.yajsw.wrapper.WrappedProcess;

// TODO: Auto-generated Javadoc
/**
 * The Class StartJob.
 */
public class StartJob extends Job
{

	WrappedProcess process;
	
	
	public StartJob(WrappedProcess wp)
	{
		process = wp;
	}


	public void run() 
	{
		if (checkStart())
		process.startByTimer();
	}

}
