package org.mgnl.nicki.consulting.survey.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class SurveyTopicWrapper {
	private SurveyTopic topic;
	private List<SurveyVote> votes;
	
	public String getName() {
		return topic.getName();
	}
	
	public String getDescription() {
		return topic.getDescription();
	}
	
	public int getVoteCount() {
		return votes != null ? votes.size() : 0;
	}
	
	public boolean isSelected(String userId, SurveyChoice choice) {
		if (votes != null) {
			for (SurveyVote vote : votes) {
				if (StringUtils.equals(userId, vote.getUserId())
						&& choice.getId() == vote.getSurveyChoiceId()) {
					return true;
				}
			}
		}
		return false;
	}

}
