package org.mgnl.nicki.consulting.survey.views;

import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.survey.model.SurveyChoice;
import org.mgnl.nicki.vaadin.base.editor.ValidationException;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.validation.Validation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

@SuppressWarnings("serial")
public class ChoiceEditor extends VerticalLayout {

	private TextField title;
	private TextArea description;
	private IntegerField weight;
	private Button saveButton;
	
	private SurveyChoice surveyChoice;


	public ChoiceEditor(SurveyChoice surveyChoice, Consumer<SurveyChoice> saveAction) {
		this.surveyChoice = surveyChoice;
		buildMainLayout();
		if (surveyChoice.getId() != null) {
			title.setValue(surveyChoice.getName());
			if (StringUtils.isNotBlank(surveyChoice.getDescription())) {
				description.setValue(surveyChoice.getDescription());
			}
			weight.setValue(surveyChoice.getWeight());
		}
		saveButton.addClickListener(event ->  {
			try {
				validate();
			} catch (ValidationException e) {
				Notification.show(e.getMessage());
			}
			this.surveyChoice.setName(title.getValue());
			this.surveyChoice.setDescription(description.getValue());
			this.surveyChoice.setWeight(weight.getValue());
			saveAction.accept(surveyChoice);
		});
	}

	
	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		title = new TextField("Bewertung");
		title.setWidthFull();
		description = new TextArea("Beschreibung");
		description.setWidthFull();
		weight = new IntegerField("Gewichtung : -3 ... -1 ... 1   3");
		weight.setWidthFull();
		saveButton = new Button("Speichern");
		add(title, description, weight, saveButton);
	}
	
	protected void validate() throws ValidationException {
		Validation.notEmpty(title, "Bitte Name eintragen");
//		Validation.notEmpty(description, "Bitte Beschreibung eintragen");
		Validation.notNull(weight, "Bitte Gewichtung eintragen");
	}
}
