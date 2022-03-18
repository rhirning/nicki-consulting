package org.mgnl.nicki.consulting.survey.model;

import java.io.Serializable;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import lombok.Data;

@Data
@Table(name="SURVEYCHOICES")
public class SurveyChoice implements Serializable {
	private static final long serialVersionUID = -669706053477027937L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "SURVEY_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=SurveyConfig.class, display="name")
	private Long surveyId;

	@Attribute(name = "NAME", mandatory = true)
	private String name;

	@Attribute(name = "DESCRIPTION", mandatory = true)
	private String description;

	@Attribute(name = "WEIGHT", mandatory = true)
	private Integer weight;
}
