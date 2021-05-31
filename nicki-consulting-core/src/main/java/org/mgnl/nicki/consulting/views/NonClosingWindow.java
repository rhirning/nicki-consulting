package org.mgnl.nicki.consulting.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;

public class NonClosingWindow extends Dialog {
	private static final long serialVersionUID = -7104786950768212140L;
	
	public NonClosingWindow() {
	}
	
		// TODO: headline
	public NonClosingWindow(String caption) {
		//super(caption);
	}
	
	public NonClosingWindow(String caption, Component component) {
		super(component);
//		super(caption, component);
	}

	@Override
	public void close() {
	}


}
