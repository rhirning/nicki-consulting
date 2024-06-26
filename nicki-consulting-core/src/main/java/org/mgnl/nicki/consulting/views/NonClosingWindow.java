package org.mgnl.nicki.consulting.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;

public class NonClosingWindow extends Dialog {
	private static final long serialVersionUID = -7104786950768212140L;
	
	public NonClosingWindow() {
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);
	}
	
	public NonClosingWindow(String caption) {
		this();
		setHeaderTitle(caption);
	}
	
	public NonClosingWindow(String caption, Component ... components ) {
		this(caption);
		add(components);
	}
}
