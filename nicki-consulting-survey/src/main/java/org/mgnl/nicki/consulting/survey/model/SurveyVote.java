package org.mgnl.nicki.consulting.survey.model;

import java.io.Serializable;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import lombok.Data;

@Data
@Table(name="SURVEYVOTES")
public class SurveyVote implements Serializable {
	private static final long serialVersionUID = -669706053477027937L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "SURVEYTOPIC_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=SurveyTopic.class, display="name")
	private Long surveyTopicId;

	@Attribute(name = "SURVEYCHOICE_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=SurveyChoice.class, display="name")
	private Long surveyChoiceId;

	@Attribute(name = "USER_ID", mandatory = true)
	private String userId;

	@Attribute(name = "COMMENT")
	private String comment;
}
