package org.mgnl.nicki.consulting.core.helper;

import java.util.List;

public class VerifyException extends Exception {
	private List<String> messages;
	public VerifyException(List<String> messages) {
		this.messages = messages;
	}

	public List<String> getMessages() {
		return messages;
	}

	private static final long serialVersionUID = -2235530866686633431L;

}
