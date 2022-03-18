package org.mgnl.nicki.consulting.survey.views;

import java.util.function.Consumer;

import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.vaadin.base.editor.ValidationException;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.validation.Validation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

@SuppressWarnings("serial")
public class TopicEditor extends VerticalLayout {

	private TextField title;
	private TextArea description;
	private Button saveButton;
	
	private SurveyTopic surveyTopic;


	public TopicEditor(SurveyTopic surveyTopic, Consumer<SurveyTopic> saveAction) {
		this.surveyTopic = surveyTopic;
		buildMainLayout();
		if (surveyTopic.getId() != null) {
			title.setValue(surveyTopic.getName());
			description.setValue(surveyTopic.getDescription());
		}
		saveButton.addClickListener(event ->  {
			try {
				validate();
			} catch (ValidationException e) {
				Notification.show(e.getMessage());
			}
			this.surveyTopic.setName(title.getValue());
			this.surveyTopic.setDescription(description.getValue());
			saveAction.accept(surveyTopic);
		});
	}

	
	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		title = new TextField("Thema");
		title.setWidthFull();
		description = new TextArea("Beschreibung");
		description.setWidthFull();
		saveButton = new Button("Speichern");
		add(title, description, saveButton);
	}
	
	protected void validate() throws ValidationException {
		Validation.notEmpty(title, "Bitte Name eintragen");
		Validation.notEmpty(description, "Bitte Beschreibung eintragen");
	}
}
