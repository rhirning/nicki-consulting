package org.mgnl.nicki.consulting.survey.model;

import java.io.Serializable;
import java.util.Date;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

import lombok.Data;

@Data
@Table(name="SURVEYCONFIG")
public class SurveyConfig implements Serializable {
	private static final long serialVersionUID = -669706053477027937L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;

	@Attribute(name = "NAME", mandatory = true)
	private String name;

	@Attribute(name = "DESCRIPTION", mandatory = true)
	private String description;

	@Attribute(name = "OWNER", mandatory = true)
	private String owner;
	
	@Attribute(name = "START_TIME", type=DataType.TIMESTAMP)
	private Date start;

	@Attribute(name = "END_TIME", type=DataType.TIMESTAMP)
	private Date end;

	@Attribute(name = "ADD_TOPIC")
	private Boolean addTopic;

}
