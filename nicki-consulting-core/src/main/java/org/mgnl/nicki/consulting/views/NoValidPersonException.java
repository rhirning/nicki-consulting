package org.mgnl.nicki.consulting.views;

public class NoValidPersonException extends Exception {
	private static final long serialVersionUID = -292443613001581964L;
	
	public NoValidPersonException(String displayName) {
		super(displayName);
	}


}
