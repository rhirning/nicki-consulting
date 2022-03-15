package org.mgnl.nicki.consulting.survey.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

@SuppressWarnings("serial")
public class TopicEditor extends VerticalLayout {

	private TextField title;
	private TextArea description;
	private Button saveButton;


	public TopicEditor(SurveyView surveyView) {
		buildMainLayout();
		saveButton.addClickListener(event -> surveyView.saveTopic(title.getValue(), description.getValue()));
	}

	
	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		title = new TextField();
		title.setWidthFull();
		description = new TextArea();
		description.setWidthFull();
		saveButton = new Button("Speichern");
		add(new Span("Thema"), title, new Span("Beschreibung"), description, saveButton);
	}
}
