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
package org.rzo.yajsw.util;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

class MyPattern implements MyPatternInterface
{
	RunAutomaton _ra;
	String _regex;

	public void setRegEx(String regex)
	{
		_regex = regex;
		RegExp re = new RegExp(regex);
		_ra = new RunAutomaton(re.toAutomaton());
	}

	public boolean matches(String input)
	{
		return _ra.run(input);
	}

	public String getRegEx()
	{
		return _regex;
	}

}
