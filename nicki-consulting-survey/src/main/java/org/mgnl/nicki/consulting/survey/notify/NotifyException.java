package org.mgnl.nicki.consulting.survey.notify;

public class NotifyException extends Exception {
	private static final long serialVersionUID = -6500778323036227238L;

	public NotifyException(String message, Exception e) {
		super(message, e);
	}

}
