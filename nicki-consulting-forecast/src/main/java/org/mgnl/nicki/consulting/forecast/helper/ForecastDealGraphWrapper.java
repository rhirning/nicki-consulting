package org.mgnl.nicki.consulting.forecast.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.forecast.model.ForecastDeal;

import com.github.appreciated.apexcharts.helper.Series;

import lombok.Data;

@Data
public class ForecastDealGraphWrapper {
	private List<LocalDate> dates;
	private List<ForecastDeal> deals;
	private Map<ForecastDeal, List<Float>> values;
	
	public Series<?>[] getSeries() {
		List<Series<?>> list = new ArrayList<>();
		for (ForecastDeal deal : deals) {
			list.add(new Series<>(deal.getName(), values.get(deal).toArray(new Float[0])));
		}
		return list.toArray(new Series[0]);
	}

}
