package org.mgnl.nicki.consulting.forecast.helper;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.forecast.model.ForecastDeal;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;

public class ForecastDealCollector {

	public enum UNIT {Personen, Umsatz};
	
	private LocalDate start;
	private LocalDate end;
	private UNIT unit = UNIT.Personen;
	private boolean ignoreProbability;
	private LocalDate time;

	public static ForecastDealCollector get() {
		return new ForecastDealCollector();
	}


	public ForecastDealCollector withStart(LocalDate start) {
		this.start = start;
		return this;
	}

	public ForecastDealCollector withEnd(LocalDate end) {
		this.end = end;
		return this;
	}

	public ForecastDealCollector withTime(LocalDate time) {
		this.time = time;
		return this;
	}

	public ForecastDealCollector withIgnoreProbability(boolean ignoreProbability) {
		this.ignoreProbability = ignoreProbability;
		return this;
	}

	public List<ForecastDeal> getDeals() {
		String filter = getFilterQuery();
		return ForecastHelper.getAllActiveDeals(filter);		
	}
	
	public ForecastDealGraphWrapper getDealGraphWrapper() {
		ForecastDealGraphWrapper dealGraphWrapper = new ForecastDealGraphWrapper();
		List<ForecastDeal> deals = getDeals();
		dealGraphWrapper.setDeals(deals);
		Date startDate = DataHelper.getDate(start);
		Date endDate = DataHelper.getDate(end);
		if (start == null) {
			for (ForecastDeal deal : deals) {
				if (startDate == null || startDate.after(deal.getStartDate())) {
					startDate = deal.getStartDate();
				}
				if (endDate == null || endDate.before(deal.getEndDate())) {
					endDate = deal.getEndDate();
				}
			}
		}
		List<LocalDate> dates = new ArrayList<>();
		
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(startDate);
		Period.setToBeginOfDay(startCalendar);
		
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(endDate);
		Period.setToBeginOfDay(endCalendar);
		
		while (startCalendar.before(endCalendar)) {
			dates.add(DataHelper.getLocalDate(startCalendar.getTime()));
			startCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		dealGraphWrapper.setDates(dates);
		
		Map<ForecastDeal, List<Float>> map = new HashMap<>();
		for (ForecastDeal deal : deals) {
			List<Float> values = new ArrayList<Float>();
			for (LocalDate date : dates) {
				if (isWeekend(date)) {
					values.add(0.0f);
				} else if (deal.getStartDate().after(DataHelper.getDate(date)) || deal.getEndDate().before(DataHelper.getDate(date))) {
					values.add(0.0f);
				} else {
					float teamSize = deal.getTeamSize();
					if (!ignoreProbability) {
						teamSize *= deal.getProbability() / 100.0f;
					}
					if (unit == UNIT.Umsatz) {
						values.add(teamSize * deal.getRate());
					} else {
						values.add(teamSize);
					}
				}
			}
			map.put(deal, values);
		}
		
		dealGraphWrapper.setValues(map);
		return dealGraphWrapper;
	}


	private boolean isWeekend(LocalDate date) {
		return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
	}


	private String getFilterQuery() {
		StringBuilder sb = new StringBuilder();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			if (time != null) {
				sb.append("VALID_FROM <= ").append(ForecastHelper.toTimestamp(time));
				sb.append(" AND (VALID_TO >= ").append(ForecastHelper.toTimestamp(time));
				sb.append(" OR VALID_TO is NULL)");
			} else {				
				sb.append("VALID_TO is NULL");
			}
			
			if (start != null) {
				sb.append(" AND START_DATE >= ").append(ForecastHelper.toDate(start));
			}
			if (end != null) {
				sb.append(" AND END_DATE > ").append(ForecastHelper.toDate(end));
			}
		} catch (SQLException e) {
		}
		return sb.toString();
	}


	public ForecastDealCollector withUnit(UNIT unit) {
		this.unit = unit;
		return this;
	}

}
