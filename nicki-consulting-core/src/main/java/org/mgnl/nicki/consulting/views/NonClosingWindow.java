package org.mgnl.nicki.consulting.views;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class NonClosingWindow extends Window {
	private static final long serialVersionUID = -7104786950768212140L;
	
	public NonClosingWindow() {
	}
	
	public NonClosingWindow(String caption) {
		super(caption);
	}
	
	public NonClosingWindow(String caption, Component component) {
		super(caption, component);
	}

	@Override
	public void close() {
	}


}
