package org.mgnl.nicki.consulting.forecast.model;

import java.io.Serializable;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.Table;

import lombok.Data;

@Data
@Table(name = "FORECAST_SALES")
public class ForecastSales implements Serializable {
	private static final long serialVersionUID = 7766174852337604799L;

	@Attribute(name = "ID", autogen = true, primaryKey = true, readonly = true)
	private Long id;

	@Attribute(name = "NAME", mandatory = true)
	private String name;

}
