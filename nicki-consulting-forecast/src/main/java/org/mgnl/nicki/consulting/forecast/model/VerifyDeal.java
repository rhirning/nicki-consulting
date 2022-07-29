package org.mgnl.nicki.consulting.forecast.model;

import java.util.ArrayList;
import java.util.List;

import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.db.verify.BeanVerifier;
import org.mgnl.nicki.db.verify.BeanVerifyError;

public class VerifyDeal implements BeanVerifier {

	@Override
	public void verify(Object bean) throws BeanVerifyError {
		ForecastDeal deal = (ForecastDeal) bean;
		
		List<String> errors = new ArrayList<>();
		
		ForecastStage stage = ForecastHelper.getStage(deal.getStageId());
		
		if (deal.getProbability() < stage.getMinProb()) {
			errors.add("Die Wahrscheinlichkeit ist zu klein");
		}
		if (deal.getProbability() > stage.getMaxProb()) {
			errors.add("Die Wahrscheinlichkeit ist zu groß");
		}

		if (errors.size() > 0) {
			throw new BeanVerifyError(errors);
		}
	}

}
