package org.mgnl.nicki.consulting.survey.views;

import java.util.List;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.GridHelper;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyConfigWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;


@SuppressWarnings("serial")
public class SurveyView extends VerticalLayout implements View {

	private Span title;
	private Span description;
	private HorizontalLayout buttonLayout;
	private Button addTopicButton;
	private Checkbox notifyCheckbox;
	private Grid<SurveyTopicWrapper> table;
	private boolean isInit;
	
	private List<SurveyTopicWrapper> surveyTopicWrappers;
	private DialogBase editWindow;
	private SurveyConfigWrapper survey;
	private Person person;


	public SurveyView(SurveyConfig survey, Person person) {
		this.survey = new SurveyConfigWrapper(survey);
		this.person = person;
		buildMainLayout();
		
		init();

	}

	public void init() {
		if (!isInit) {
			title.setText(survey.getName());
			description.setText(survey.getDescription());
			table.addColumn(SurveyTopicWrapper::getName);

			table.setItemDetailsRenderer(new TextRenderer<>(t -> t.getDescription()));
			GridHelper.addSurveyTopicWrapperColumns(table, survey);
			table.setSizeFull();
			table.setAllRowsVisible(true);
			
			if (survey.getAddTopic()) {
				addTopicButton.addClickListener(event -> showTopicEditor());
			} else {
				addTopicButton.setVisible(false);
			}
			if (survey.getAddTopic()) {
				notifyCheckbox.setValue(SurveyHelper.getNotify(survey, person));
				notifyCheckbox.addValueChangeListener(event -> {
					if (event.getValue()) {
						SurveyHelper.addNotify(survey, person);
					} else {
						SurveyHelper.removeNotify(survey, person);
					}
				});
			} else {
				notifyCheckbox.setVisible(false);
			}
			isInit = true;
		}
		collectData();
		if (surveyTopicWrappers != null) {
			table.setItems(surveyTopicWrappers);
		}
	}

	private void showTopicEditor() {
		SurveyTopic surveyTopic = new SurveyTopic();
		surveyTopic.setOwner(person.getUserId());
		surveyTopic.setSurveyId(survey.getId());
		TopicEditor topicEditor = new TopicEditor(surveyTopic, t -> {
			survey.addTopic(t);
			editWindow.close();
			SurveyHelper.updateNotifies(survey.getSurveyConfig(), person);
			init();
		});
		editWindow = new DialogBase("Neues Thema", topicEditor);
		editWindow.setModal(true);
		editWindow.setWidth("600px");
		editWindow.setHeight("600px");
		editWindow.open();
	}


	public void saveTopic(String title, String description) {
		survey.addTopic(person, title, description);
		editWindow.close();
		init();
		
	}

	private void collectData() {
		surveyTopicWrappers = SurveyHelper.getTopicWrappers(survey.getSurveyConfig(), person);
	}

	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		title = new Span();
		description = new Span();
		buttonLayout = new HorizontalLayout();
		addTopicButton = new Button("Neues Thema");
		notifyCheckbox = new Checkbox("Benachrichtigen");
		buttonLayout.add(addTopicButton, notifyCheckbox);
		
		table = new Grid<>();
		table.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		add(title, description, buttonLayout, table);
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setApplication(NickiApplication arg0) {
		// TODO Auto-generated method stub
		
	}

}
