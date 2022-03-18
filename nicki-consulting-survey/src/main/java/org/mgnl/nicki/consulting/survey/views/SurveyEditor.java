package org.mgnl.nicki.consulting.survey.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.GridHelper;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyChoice;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.core.helper.NameValue;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.editor.ValidationException;
import org.mgnl.nicki.vaadin.base.validation.Validation;

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Slf4j
public class SurveyEditor extends HorizontalLayout {
	
	private VerticalLayout basicLayout;
	private TextField idTextField;
	private TextField ownerTextField;
	private TextField titleTextField;
	private TextArea descriptionTextArea;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private DatePicker visibleDatePicker;
	private Checkbox enableAddTopicCheckbox;
	private Checkbox enableAddCommentCheckbox;
	private HorizontalLayout buttonLayout;
	private Button saveButton;
	private Button cancelButton;
	

	private HorizontalLayout topicsChoicesButtonLayout;
	private Button topicsButton;
	private Button choicesButton;
	
	private VerticalLayout topicsLayout;
	private Grid<SurveyTopic> topicsTable;
	private HorizontalLayout topicsButtonLayout;
	private Button addTopicButton;
	private Button editTopicButton;
	
	private VerticalLayout choicesLayout;
	private Grid<SurveyChoice> choicesTable;
	private HorizontalLayout choicesButtonLayout;
	private Button addChoiceButton;
	private Button editChoiceButton;

	private VerticalLayout canvas;
	
	private Person person;
	private SurveyConfigView surveyConfigView;
	private SurveyConfig surveyConfig;
	private boolean create;
	private List<SurveyTopic> topics;
	private List<SurveyChoice> choices;
	private DialogBase editWindow;

	public SurveyEditor(Person person, SurveyConfigView surveyConfigView, Optional<SurveyConfig> surveyConfigOptional) {
		this.person = person;
		this.surveyConfigView = surveyConfigView;
		if (surveyConfigOptional.isPresent()) {
			this.surveyConfig = surveyConfigOptional.get();
		} else {
			this.surveyConfig = new SurveyConfig();
			this.surveyConfig.setOwner(person.getUserId());
			this.surveyConfig.setAddTopic(true);
			create = true;
		}

		this.topics = SurveyHelper.getTopics(surveyConfig);
		this.choices = SurveyHelper.getChoices(surveyConfig);
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
		cancelButton.addClickListener(event -> {
			surveyConfigView.init();
		});
		
		
		
		
		if (create) {
			ownerTextField.setValue(person.getUserId());
			topicsChoicesButtonLayout.setVisible(false);
		} else {
			initTopics();
			initChoices();
			
			idTextField.setValue(Long.toString(surveyConfig.getId()));
			ownerTextField.setValue(surveyConfig.getOwner());
			topicsButton.addClickListener(event -> showTopics());
			choicesButton.addClickListener(event -> showChoices());
		}
		ownerTextField.setValue(surveyConfig.getOwner());
		if (StringUtils.isNotBlank(surveyConfig.getName())) {
			titleTextField.setValue(surveyConfig.getName());
		}
		if (StringUtils.isNotBlank(surveyConfig.getDescription())) {
			descriptionTextArea.setValue(surveyConfig.getDescription());
		}
		startDatePicker.setValue(DataHelper.getLocalDate(surveyConfig.getStart()));
		endDatePicker.setValue(DataHelper.getLocalDate(surveyConfig.getEnd()));
		enableAddTopicCheckbox.setValue(surveyConfig.getAddTopic());
		if (surveyConfig.getAddComment() != null) {
			enableAddCommentCheckbox.setValue(surveyConfig.getAddComment());
		}
		
	}

	
	private void initTopics() {
		topicsButtonLayout = new HorizontalLayout();
		addTopicButton = new Button("Neues Thema");
		editTopicButton = new Button("Thema ändern");
		editTopicButton.setVisible(false);
		topicsButtonLayout.add(addTopicButton, editTopicButton);
		addTopicButton.addClickListener(event -> {
			SurveyTopic topic = new SurveyTopic();
			topic.setSurveyId(surveyConfig.getId());
			topic.setOwner(person.getUserId());
			showTopicEditor(topic);
		});
		editTopicButton.addClickListener(event -> {
			SurveyTopic topic = topicsTable.getSelectedItems().iterator().next();
			showTopicEditor(topic);
		});
		topicsTable = new Grid<>();
		topicsLayout.add(topicsButtonLayout, topicsTable);
		topicsTable.addColumn(SurveyTopic::getName);
		topicsTable.setItemDetailsRenderer(new ComponentRenderer<>(t -> getDetails(t)));
		
		topicsTable.addSelectionListener(event -> {
			Optional<SurveyTopic> itemOptional = event.getFirstSelectedItem();
			if (itemOptional.isPresent()) {
				editTopicButton.setVisible(true);
			} else {
				editTopicButton.setVisible(false);
			}
		});
	}
	
	private void initChoices() {
		choicesButtonLayout = new HorizontalLayout();
		addChoiceButton = new Button("Neue Auswahl");
		editChoiceButton = new Button("Auswahl ändern");
		editChoiceButton.setVisible(false);
		choicesButtonLayout.add(addChoiceButton, editChoiceButton);
		addChoiceButton.addClickListener(event -> {
			SurveyChoice choice = new SurveyChoice();
			choice.setSurveyId(surveyConfig.getId());
			showChoiceEditor(choice);
		});
		editChoiceButton.addClickListener(event -> {
			SurveyChoice choice = choicesTable.getSelectedItems().iterator().next();
			showChoiceEditor(choice);
		});
		choicesTable = new Grid<>();
		choicesLayout.add(choicesButtonLayout, choicesTable);
		choicesTable.addColumn(SurveyChoice::getName);
		choicesTable.setItemDetailsRenderer(new ComponentRenderer<>(c -> getDetails(c)));
		
		choicesTable.addSelectionListener(event -> {
			Optional<SurveyChoice> itemOptional = event.getFirstSelectedItem();
			if (itemOptional.isPresent()) {
				editChoiceButton.setVisible(true);
			} else {
				editChoiceButton.setVisible(false);
			}
		});
	}
	
	private Grid<NameValue> getDetails(SurveyChoice survey) {
		List<NameValue> values = new ArrayList<>();
		values.add(new NameValue("Beschreibung", survey.getDescription()));
		values.add(new NameValue("Gewichtung", Integer.toString(survey.getWeight())));
		return GridHelper.getDetails(values);
	}
	
	private Grid<NameValue> getDetails(SurveyTopic topic) {
		List<NameValue> values = new ArrayList<>();
		values.add(new NameValue("Beschreibung", topic.getDescription()));
		return GridHelper.getDetails(values);
	}
	
	private void showChoiceEditor(SurveyChoice choice) {
		String windowTitle;
		if (choice.getId() == null) {
			windowTitle = "Neue Auswahl";
		} else {
			windowTitle = "Auswahl ändern";
		}
		ChoiceEditor choiceEditor = new ChoiceEditor(choice, c -> {
			try {
				if (c.getId() == null) {
					SurveyHelper.create(c);
					editWindow.close();
				} else {
					SurveyHelper.update(c);
					editWindow.close();
				}
				this.choices = SurveyHelper.getChoices(surveyConfig);
				choicesTable.setItems(choices);
			} catch (SQLException | InitProfileException | NotSupportedException e) {
				Notification.show("Could not save or update topic: " + c);
			}
		});
		editWindow = new DialogBase(windowTitle, choiceEditor);
		editWindow.setModal(true);
		editWindow.setWidth("600px");
		editWindow.setHeight("600px");
		editWindow.open();
	}
	
	private void showTopicEditor(SurveyTopic surveyTopic) {
		String windowTitle;
		if (surveyTopic.getId() == null) {
			windowTitle = "Neues Thema";
		} else {
			windowTitle = "Thema ändern";
		}
		TopicEditor topicEditor = new TopicEditor(surveyTopic, s -> {
			try {
				if (s.getId() == null) {
					SurveyHelper.create(s);
					editWindow.close();
				} else {
					SurveyHelper.update(s);
					editWindow.close();
				}
				this.topics = SurveyHelper.getTopics(surveyConfig);
				topicsTable.setItems(topics);
			} catch (SQLException | InitProfileException | NotSupportedException e) {
				Notification.show("Could not save or update topic: " + s);
			}
		});
		editWindow = new DialogBase(windowTitle, topicEditor);
		editWindow.setModal(true);
		editWindow.setWidth("600px");
		editWindow.setHeight("600px");
		editWindow.open();
	}

	private void showChoices() {
		canvas.removeAll();
		canvas.add(choicesLayout);
		choicesTable.setItems(choices);
	}


	private void showTopics() {
		canvas.removeAll();
		canvas.add(topicsLayout);
		topicsTable.setItems(topics);
	}


	private void save() throws SQLException, InitProfileException, NotSupportedException {
		try {
			validate();
		} catch (ValidationException e) {
			Notification.show(e.getMessage());
		}
		surveyConfig.setOwner(ownerTextField.getValue());
		surveyConfig.setName(titleTextField.getValue());
		surveyConfig.setDescription(descriptionTextArea.getValue());
		surveyConfig.setStart(DataHelper.getDate(startDatePicker.getValue()));
		surveyConfig.setEnd(DataHelper.getDate(endDatePicker.getValue()));
		surveyConfig.setVisible(DataHelper.getDate(visibleDatePicker.getValue()));
		surveyConfig.setAddTopic(enableAddTopicCheckbox.getValue());
		surveyConfig.setAddComment(enableAddCommentCheckbox.getValue());
		if (create) {
			SurveyHelper.create(surveyConfig);
		} else {
			SurveyHelper.update(surveyConfig);
		}
	}


	private void buildMainLayout() {
		setSizeFull();
		basicLayout = new VerticalLayout();
		basicLayout.setHeightFull();
		String columnWidth = surveyConfigView.getConfiguration() != null && surveyConfigView.getConfiguration().get("columnWidth") != null ? surveyConfigView.getConfiguration().get("columnWidth") : "400px";

		basicLayout.setWidth(columnWidth);
		idTextField = new TextField("ID");
		idTextField.setEnabled(false);
		ownerTextField = new TextField("Owner");
		ownerTextField.setEnabled(false);
		
		titleTextField = new TextField("Name");
		descriptionTextArea = new TextArea("Beschreibung");
		startDatePicker = new DatePicker("Start");
		endDatePicker = new DatePicker("Ende");
		visibleDatePicker = new DatePicker("Sichtbar");
		enableAddTopicCheckbox = new Checkbox("Themen ergänzen?");
		enableAddCommentCheckbox = new Checkbox("Kommentar");
		buttonLayout = new HorizontalLayout();
		saveButton = new Button("Speichern");
		cancelButton = new Button("Abbrechen");
		buttonLayout.add(saveButton, cancelButton);
		topicsChoicesButtonLayout = new HorizontalLayout();
		topicsButton = new Button("Themen");
		choicesButton = new Button("Auswahl");
		topicsChoicesButtonLayout.add(topicsButton, choicesButton);
		
		basicLayout.add(idTextField, ownerTextField, titleTextField, descriptionTextArea, startDatePicker, endDatePicker, visibleDatePicker, enableAddTopicCheckbox, enableAddCommentCheckbox, buttonLayout, topicsChoicesButtonLayout);
		
		canvas = new VerticalLayout();
		topicsLayout = new VerticalLayout();
		choicesLayout = new VerticalLayout();

		
		add(basicLayout, canvas);
	}

	
	protected void validate() throws ValidationException {
		Validation.notEmpty(titleTextField, "Bitte Name eintragen");
		Validation.notEmpty(descriptionTextArea, "Bitte Beschreibung eintragen");
	}
}
