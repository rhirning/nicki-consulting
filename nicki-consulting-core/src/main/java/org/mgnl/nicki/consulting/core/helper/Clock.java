package org.mgnl.nicki.consulting.core.helper;

import org.apache.commons.lang.StringUtils;

public class Clock {
	private int hours;
	private int minutes;
	
	public Clock(int hours, int minutes) {
		super();
		this.hours = hours;
		this.minutes = minutes;
	}

	public static Clock parse(String data) throws DateFormatException {
		int hours = 0;
		int minutes = 0;
		try {
			if (StringUtils.contains(data, ":")) {
				String hoursString = StringUtils.stripToEmpty(StringUtils.substringBefore(data, ":"));
				String minutesString = StringUtils.stripToEmpty(StringUtils.substringAfter(data, ":"));
				if (StringUtils.isNotBlank(hoursString)) {
					hours = Integer.parseInt(hoursString);
				}
				if (StringUtils.isNotBlank(minutesString)) {
					minutes = Integer.parseInt(minutesString);
				}
			} else if (StringUtils.isNumeric(data) && data.length() == 4) {
				String hoursString = StringUtils.stripToEmpty(StringUtils.substring(data, 0, 2));
				String minutesString = StringUtils.stripToEmpty(StringUtils.substring(data, 2, 4));
				hours = Integer.parseInt(hoursString);
				minutes = Integer.parseInt(minutesString);
			} else {
				hours = Integer.parseInt(StringUtils.stripToEmpty(data));
			}
		} catch (NumberFormatException e) {
			throw new DateFormatException("Invalid time format: " + data);
		}
		if (hours < 0 || hours > 23) {
			throw new DateFormatException("Invalid hours: " + data);
		}
		if (minutes < 0 || minutes > 59) {
			throw new DateFormatException("Invalid minutes: " + data);
		}
		return new Clock(hours, minutes);
	}

	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}
}
