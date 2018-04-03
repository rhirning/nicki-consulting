package org.mgnl.nicki.consulting.views;

import java.util.Calendar;
import java.util.Date;

import org.mgnl.nicki.core.i18n.I18n;

public enum PERIOD {
	LAST_MONTH {
		@Override
		public Date getStart() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.add(Calendar.MONTH, -1);
			return calendar.getTime();
		}

		@Override
		public Date getEnd() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			return calendar.getTime();
		}
	},
	THIS_MONTH {
		@Override
		public Date getStart() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			return calendar.getTime();
		}

		@Override
		public Date getEnd() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.add(Calendar.MONTH, 1);
			return calendar.getTime();
		}
	},
	LAST_YEAR {
		@Override
		public Date getStart() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.MONTH, 0);
			calendar.add(Calendar.YEAR, -1);
			return calendar.getTime();
		}

		@Override
		public Date getEnd() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.MONTH, 0);
			return calendar.getTime();
		}
	},
	THIS_YEAR {
		@Override
		public Date getStart() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.MONTH, 0);
			return calendar.getTime();
		}

		@Override
		public Date getEnd() {
			Calendar calendar = getTodayCalendar();
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.MONTH, 0);
			calendar.add(Calendar.YEAR, 1);
			return calendar.getTime();
		}
	};

	public String getName() {
		return I18n.getText("nicki.consulting.period." + name());
	}

	protected Calendar getTodayCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public abstract Date getStart();

	public abstract Date getEnd();


	public static void setDay(Date date, Calendar newDay) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, newDay.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, newDay.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, newDay.get(Calendar.DAY_OF_MONTH));
		
		date.setTime(calendar.getTime().getTime());
	}

	public boolean matches(Date start) {
		return getStart().before(start) && getEnd().after(start);
	}
}
