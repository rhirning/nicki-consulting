package org.mgnl.nicki.consulting.forecast.model;

import java.io.Serializable;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.Table;

import lombok.Data;

@Data
@Table(name = "FORECAST_STAGES")
public class ForecastStage implements Serializable {
	private static final long serialVersionUID = -3433855773872365202L;

	@Attribute(name = "ID", autogen = true, primaryKey = true, readonly = true)
	private Long id;

	@Attribute(name = "NAME", mandatory = true)
	private String name;
	
	@Attribute(name = "MIN_PROB")
	private Integer minProb;
	
	@Attribute(name = "MAX_PROB")
	private Integer maxProb;
	
	@Attribute(name = "POSITION")
	private Integer position;

}
