package org.mgnl.nicki.consulting.forecast.model;

import java.io.Serializable;

import org.mgnl.nicki.core.data.TreeObject;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.Table;

import lombok.Data;

@Data
@Table(name = "FORECAST_CUSTOMERS")
public class ForecastCustomer implements Serializable, TreeObject {
	private static final long serialVersionUID = 5053669272654590878L;

	@Attribute(name = "ID", autogen = true, primaryKey = true, readonly = true)
	private Long id;

	@Attribute(name = "NAME", mandatory = true)
	private String name;

	@Attribute(name = "ALIAS")
	private String alias;

	@Attribute(name = "STREET")
	private String street;

	@Attribute(name = "ZIP")
	private String zip;

	@Attribute(name = "CITY")
	private String city;

	@Override
	public String getDisplayName() {
		return name;
	}

}
