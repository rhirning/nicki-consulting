package org.mgnl.nicki.consulting.forecast.model;

import java.sql.SQLException;
import java.util.Date;

import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.db.verify.BeanUpdater;
import org.mgnl.nicki.db.verify.UpdateBeanException;

public class UpdateDeal implements BeanUpdater {

	@Override
	public void update(DBContext dbContext, Object bean) throws UpdateBeanException {
		ForecastDeal deal = (ForecastDeal) bean;
		Date now = new Date();
		try {
			ForecastDeal oldDeal = ForecastHelper.getDeal(deal.getId());
			oldDeal.setId(null);
			oldDeal.setValidTo(now);
			dbContext.create(oldDeal);
			
			deal.setValidFrom(now);
			dbContext.update(deal);
		} catch (NotSupportedException | SQLException | InitProfileException e) {
			throw new UpdateBeanException(e);
		}
	}
}
