package org.mgnl.nicki.consulting.views;

import java.util.Calendar;
import java.util.Date;

import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.i18n.I18n;

public enum PERIOD {
	LAST_MONTH {
		@Override
		public void init() {
			Calendar start = Period.getFirstDayOfMonth();
			start.add(Calendar.MONTH, -1);

			this.period = new Period(start, Period.getFirstDayOfMonth());
		}
	},
	THIS_MONTH {
		@Override
		public void init() {
			this.period = new Period(Period.getFirstDayOfMonth(), Period.getLastDayOfMonth());
		}
	},
	LAST_YEAR {
		@Override
		public void init() {
			Calendar start = Period.getFirstDayOfYear();
			start.add(Calendar.YEAR, -1);

			
			this.period = new Period(start, Period.getFirstDayOfYear());
		}
	},
	THIS_YEAR {
		@Override
		public void init() {
			this.period = new Period(Period.getFirstDayOfYear(), Period.getLastDayOfYear());
		}
	};
	
	Period period;
	
	PERIOD() {
		init();
	}
	
	public Calendar getStart() {
		return this.period.getStart();
	}

	public Calendar getEnd() {
		return this.period.getEnd();
	}

	public String getName() {
		return I18n.getText("nicki.consulting.period." + name());
	}

	public abstract void init();

	public boolean matches(Date start) {
		return this.period.matches(start);
	}

}