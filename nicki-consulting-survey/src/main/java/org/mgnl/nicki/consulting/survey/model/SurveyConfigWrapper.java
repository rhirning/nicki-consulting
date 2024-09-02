package org.mgnl.nicki.consulting.survey.model;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;

import lombok.Data;

@Data
public class SurveyConfigWrapper implements Serializable {
	private static final long serialVersionUID = 7747170285870299985L;
	
	private SurveyConfig surveyConfig;
	private List<SurveyTopic> topics;
	private List<SurveyChoice> choices;
	private String[] titles;
	private String[] descriptions;
	
	public SurveyConfigWrapper(SurveyConfig surveyConfig) {
		this.surveyConfig = surveyConfig;
		topics = SurveyHelper.getTopics(surveyConfig);
		choices = SurveyHelper.getChoices(surveyConfig);
		titles = choices.stream().map(c -> StringUtils.stripToEmpty(c.getName())).collect(Collectors.toList()).toArray(new String[0]);
		descriptions = choices.stream().map(c -> StringUtils.stripToEmpty(c.getDescription())).collect(Collectors.toList()).toArray(new String[0]);
	}

	public String getName() {
		return surveyConfig.getName();
	}

	public String getDescription() {
		return surveyConfig.getDescription();
	}

	public boolean getAddTopic() {
		return surveyConfig.getAddTopic() != null && surveyConfig.getAddTopic().booleanValue();
	}

	public boolean getAddComment() {
		return surveyConfig.getAddComment() != null && surveyConfig.getAddComment().booleanValue();
	}

	public Long getId() {
		return surveyConfig.getId();
	}

	public void addTopic(Person person, String title, String description) {
		SurveyHelper.addTopic(surveyConfig, person, title, description);
		topics = SurveyHelper.getTopics(surveyConfig);		
	}

	public void addTopic(SurveyTopic topic) {
		SurveyHelper.addTopic(surveyConfig, topic.getOwner(), topic.getName(), topic.getDescription());		
	}
}
