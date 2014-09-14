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
		_ra  = new RunAutomaton(re.toAutomaton());
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
