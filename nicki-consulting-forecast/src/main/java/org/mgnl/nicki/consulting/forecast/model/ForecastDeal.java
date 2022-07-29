package org.mgnl.nicki.consulting.forecast.model;

import java.io.Serializable;
import java.util.Date;

import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.core.data.TreeObject;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

import lombok.Data;

@Data
@Table(name="FORECAST_DEALS", verifyClass = VerifyDeal.class, updateClass = UpdateDeal.class)
public class ForecastDeal implements Serializable, TreeObject {
	private static final long serialVersionUID = -2214733525586227887L;

	@Attribute(name = "ID", autogen=true, primaryKey=true, readonly = true)
	private Long id;
	
	@Attribute(name = "CUSTOMER_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=ForecastCustomer.class, display = "name")
	private Long customerId;
	
	@Attribute(name = "STAGE_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=ForecastStage.class, display = "name")
	private Long stageId;
	
	@Attribute(name = "NAME", mandatory = true)
	private String name;
	
	@Attribute(name = "DESCRIPTION")
	private String description;
	
	@Attribute(name = "CONTACT")
	private String contact;
	
	@Attribute(name = "CATEGORY_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=ForecastCategory.class, display = "name")
	private Long categoryId;
	
	@Attribute(name = "SALES_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=ForecastSales.class, display = "name")
	private Long salesId;

	@Attribute(name = "START_DATE", type=DataType.DATE, mandatory = true)
	private Date startDate;

	@Attribute(name = "END_DATE", type=DataType.DATE, mandatory = true)
	private Date endDate;

	@Attribute(name = "TEAM_SIZE", mandatory = true)
	private Integer teamSize;

	@Attribute(name = "RATE")
	private Float rate;

	@Attribute(name = "PROBABILITY", mandatory = true)
	private Float probability;

	@Attribute(name = "VALID_FROM", type=DataType.TIMESTAMP, now = true)
	private Date validFrom;

	@Attribute(name = "VALID_TO", type=DataType.TIMESTAMP)
	private Date validTo;

	@Override
	public String getDisplayName() {
		return name;
	}
	
	public ForecastCustomer getCustomer() {
		return ForecastHelper.getCustomer(customerId);
	}

	public ForecastStage getStage() {
		return ForecastHelper.getStage(stageId);
	}
}
