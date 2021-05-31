package org.mgnl.nicki.consulting.core.model;

public enum Pause {
	MIN_0(0, "keine"),
	MIN_30(30, "30 Min."),
	MIN_60(60, "60 Min.")
	;
	private int mins;
	private String displayName;
	
	Pause(int mins, String displayName) {
		this.mins = mins;
		this.displayName = displayName;
	}

	public int getMins() {
		return mins;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static Pause getPause(Integer mins) {
		for (Pause pause : values())
			if (pause.mins == mins) {
				return pause;
			}
		return null;
	}
	
}
