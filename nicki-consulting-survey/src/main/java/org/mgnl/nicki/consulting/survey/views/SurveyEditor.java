package org.mgnl.nicki.consulting.survey.views;

import java.sql.SQLException;

import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Slf4j
public class SurveyEditor extends HorizontalLayout {
	
	private VerticalLayout basicLayout;
	private TextField idTextField;
	private TextField titleTextField;
	private TextArea descriptionTextArea;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private Checkbox addTopicCheckbox;
	private Button saveButton;

	private VerticalLayout canvas;
	
	private SurveyConfigView surveyConfigView;
	private SurveyConfig surveyConfig;
	private boolean create;

	public SurveyEditor(SurveyConfigView surveyConfigView, SurveyConfig surveyConfig) {
		this.surveyConfigView = surveyConfigView;
		if (surveyConfig == null) {
			this.surveyConfig = new SurveyConfig();
			create = true;
		} else {
			this.surveyConfig = surveyConfig;
		}
		buildMainLayout();
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		saveButton.addClickListener(event -> {
			try {
				save();
			} catch (SQLException | InitProfileException | NotSupportedException e) {
				Notification.show("Error saving SurveyConfig: " + surveyConfig);
				log.error("Error saving SurveyConfig: " + surveyConfig, e);
			}
			surveyConfigView.init();
		});
	}

	
	private void save() throws SQLException, InitProfileException, NotSupportedException {
		surveyConfig.setName(titleTextField.getValue());
		surveyConfig.setDescription(descriptionTextArea.getValue());
		surveyConfig.setStart(DataHelper.getDate(startDatePicker.getValue()));
		surveyConfig.setEnd(DataHelper.getDate(endDatePicker.getValue()));
		surveyConfig.setAddTopic(addTopicCheckbox.getValue());
		if (create) {
			SurveyHelper.create(surveyConfig);
		} else {
			SurveyHelper.update(surveyConfig);
		}
	}


	private void buildMainLayout() {
		basicLayout = new VerticalLayout();
		basicLayout.setHeightFull();
		String columnWidth = surveyConfigView.getConfiguration() != null && surveyConfigView.getConfiguration().get("columnWidth") != null ? surveyConfigView.getConfiguration().get("columnWidth") : "400px";

		basicLayout.setWidth(columnWidth);
		idTextField = new TextField("ID");
		idTextField.setEnabled(false);
		
		titleTextField = new TextField("Name");
		descriptionTextArea = new TextArea("Beschreibung");
		startDatePicker = new DatePicker("Start");
		endDatePicker = new DatePicker("End");
		addTopicCheckbox = new Checkbox("Themen ergänzen?");
		saveButton = new Button("Speichern");
		
		basicLayout.add(idTextField, titleTextField, descriptionTextArea, startDatePicker, endDatePicker, addTopicCheckbox, saveButton);
		
		canvas = new VerticalLayout();
		
		add(basicLayout, canvas);
	}
}
