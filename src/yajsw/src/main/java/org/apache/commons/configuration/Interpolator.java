package org.apache.commons.configuration;

import groovy.lang.Binding;

import java.util.Map;

public interface Interpolator
{
	public Object interpolate(Object name);

	public Binding getBinding();

	public Map<? extends String, ? extends String> getFromBinding();

	public Map<String, String> getUsedEnvVars();

}
