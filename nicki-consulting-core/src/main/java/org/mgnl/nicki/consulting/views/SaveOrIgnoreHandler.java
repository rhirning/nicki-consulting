package org.mgnl.nicki.consulting.views;

import org.mgnl.nicki.consulting.views.SaveOrIgnoreDialog.DECISION;

public interface SaveOrIgnoreHandler {
	void handle(DECISION decision);
}
