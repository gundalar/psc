package org.dynamicjava.osgi.da_launcher.internal.exceptions;

public class ConfigurationException extends LauncherException {
	
	private static final long serialVersionUID = ("urn:" + ConfigurationException.class.getName()).hashCode();
	
	public ConfigurationException() {
		super();
	}
	
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ConfigurationException(String message) {
		super(message);
	}
	
	public ConfigurationException(Throwable cause) {
		super(cause);
	}
	
}
