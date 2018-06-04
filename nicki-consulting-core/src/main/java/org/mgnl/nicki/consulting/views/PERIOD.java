package org.mgnl.nicki.consulting.views;

import java.util.Calendar;
import java.util.Date;

import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.i18n.I18n;

public enum PERIOD {
	LAST_MONTH {
		@Override
		public Period getPeriod() {
			Calendar start = Period.getFirstDayOfMonth();
			start.add(Calendar.MONTH, -1);

			return new Period(start, Period.getFirstDayOfMonth());
		}
	},
	THIS_MONTH {
		@Override
		public Period getPeriod() {
			return new Period(Period.getFirstDayOfMonth(), Period.getLastDayOfMonth());
		}
	},
	LAST_YEAR {
		@Override
		public Period getPeriod() {
			Calendar start = Period.getFirstDayOfYear();
			start.add(Calendar.YEAR, -1);

			
			return new Period(start, Period.getFirstDayOfYear());
		}
	},
	THIS_YEAR {
		@Override
		public Period getPeriod() {
			return new Period(Period.getFirstDayOfYear(), Period.getLastDayOfYear());
		}
	};
	
	public Calendar getStart() {
		return this.getPeriod().getStart();
	}

	public Calendar getEnd() {
		return this.getPeriod().getEnd();
	}

	public String getName() {
		return I18n.getText("nicki.consulting.period." + name());
	}

	public boolean matches(Date start) {
		return this.getPeriod().matches(start);
	}

	public abstract Period getPeriod();

}