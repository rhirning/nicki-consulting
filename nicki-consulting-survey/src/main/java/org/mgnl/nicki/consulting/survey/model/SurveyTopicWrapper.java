package org.mgnl.nicki.consulting.survey.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import lombok.Getter;

public class SurveyTopicWrapper {
	private @Getter SurveyTopic topic;
	private Optional<SurveyVote> vote;
	private List<SurveyVote> votes;
	private Person person;
	private SurveyConfig surveyConfig;
	private List<Checkbox> checkboxes;
	private List<Long> voteCounts;
	private TextArea commentTextArea;
	private @Getter int value;
	
	public SurveyTopicWrapper(SurveyConfig surveyConfig, SurveyTopic topic, Person person) {
		this.surveyConfig = surveyConfig;
		this.topic = topic;
		this.person = person;
		init();
	}
	
	private void init() {
		this.vote = SurveyHelper.getSurveyVote(topic, person);
		this.votes = SurveyHelper.getSurveyVotes(topic);
		checkboxes = new ArrayList<Checkbox>();
		voteCounts = new ArrayList<>();
		for (SurveyChoice choice : SurveyHelper.getChoices(surveyConfig)) {
			Checkbox checkbox = new Checkbox();
			ComponentUtil.setData(checkbox, "choiceId", choice.getId());
			checkbox.setValue(isChecked(choice));
			checkbox.addValueChangeListener(event -> {
				Checkbox c = event.getSource();
				if (event.getValue()) {
					Long choiceId = (Long) ComponentUtil.getData(c, "choiceId");
					clearOtherChoices(choiceId);
					SurveyHelper.updateVote(topic, person, choiceId);
				} else {
					SurveyHelper.removeVote(topic, person);
				}
				this.vote = SurveyHelper.getSurveyVote(topic, person);
			});
			
			checkboxes.add(checkbox);
			
			voteCounts.add(SurveyHelper.countVotes(topic, choice));
			value = SurveyHelper.getValue(topic);
		}
		
	}

	private String getComment() {
		return commentTextArea != null ? commentTextArea.getValue() : "";
	}

	private TextArea getCommentComponent() {
		if (commentTextArea == null) {
			commentTextArea = new TextArea();
			if (this.vote.isPresent() && this.vote.get().getComment() != null) {
				commentTextArea.setValue(this.vote.get().getComment());
			}
			commentTextArea.addValueChangeListener(event -> {
				SurveyHelper.updateVoteComment(topic, person, getComment());
			});
		}
		return commentTextArea;
	}
	
	private void clearOtherChoices(Long choiceId) {
		for (Checkbox checkbox :checkboxes) {

			Long id = (Long) ComponentUtil.getData(checkbox, "choiceId");
			if (choiceId != id) {
				checkbox.setValue(false);
			}
		}
	}

	private boolean isChecked(SurveyChoice choice) {
		return vote.isPresent() && vote.get().getSurveyChoiceId() == choice.getId();
	}

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
	
	public HorizontalLayout getVoteComponent() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();
		
		
		return layout;
	}

	public Checkbox getColumn(int i) {
		if (i < checkboxes.size()) {
			return checkboxes.get(i);
		} else {
			return null;
		}
	}
	
	private Span getSummaryColumn(int i) {
		return new Span(Long.toString(voteCounts.get(i)));
	}
	
	public Span getSummary0() {
		return getSummaryColumn(0);
	}
	
	public Span getSummary1() {
		return getSummaryColumn(1);
	}
	
	public Span getSummary2() {
		return getSummaryColumn(2);
	}
	
	public Span getSummary3() {
		return getSummaryColumn(3);
	}
	
	public Span getSummary4() {
		return getSummaryColumn(4);
	}
	
	public Span getSummary5() {
		return getSummaryColumn(5);
	}
	
	public Span getSummary6() {
		return getSummaryColumn(6);
	}
	
	public Span getSummary7() {
		return getSummaryColumn(7);
	}
	
	public Span getSummary8() {
		return getSummaryColumn(8);
	}
	
	public Span getSummary9() {
		return getSummaryColumn(9);
	}

	public Checkbox get0() {
		return getColumn(0);
	}
	
	public Checkbox get1() {
		return getColumn(1);
	}
	
	public Checkbox get2() {
		return getColumn(2);
	}
	
	public Checkbox get3() {
		return getColumn(3);
	}
	
	public Checkbox get4() {
		return getColumn(4);
	}
	
	public Checkbox get5() {
		return getColumn(5);
	}
	
	public Checkbox get6() {
		return getColumn(6);
	}
	
	public Checkbox get7() {
		return getColumn(7);
	}
	
	public Checkbox get8() {
		return getColumn(8);
	}
	
	public Checkbox get9() {
		return getColumn(9);
	}
	
	public TextArea get10() {
		return getCommentComponent();
	}
	

}
