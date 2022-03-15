package org.mgnl.nicki.consulting.survey.views;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyVote;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.core.util.Classes;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.data.renderer.TextRenderer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class SurveyView extends VerticalLayout implements View {

	private Span title;
	private Span description;
	private HorizontalLayout buttonLayout;
	private Button addTopicButton;
	private Button saveButton;
	private Checkbox notifyCheckbox;
	private Grid<SurveyTopicWrapper> table;
	private boolean isInit;
	
	private List<SurveyTopicWrapper> surveyTopicWrappers;
	private DialogBase editWindow;
	private SurveyConfig survey;
	private Person person;


	public SurveyView(SurveyConfig survey, Person person) {
		this.survey = survey;
		this.person = person;
		buildMainLayout();
		
		init();

	}

	public void init() {
		if (!isInit) {
			title.setText(survey.getName());
			description.setText(survey.getDescription());
			table.addColumn(SurveyTopicWrapper::getName);
			table.setItemDetailsRenderer(new TextRenderer<>(SurveyTopicWrapper::getDescription));
			table.setSizeFull();
			table.setAllRowsVisible(true);
			
			if (survey.getAddTopic() != null && survey.getAddTopic()) {
				addTopicButton.addClickListener(event -> showTopicEditor());
			} else {
				addTopicButton.setVisible(false);
			}
			
			isInit = true;
		}
		collectData();
		if (surveyTopicWrappers != null) {
			table.setItems(surveyTopicWrappers);
		}
	}

	private void showTopicEditor() {
		TopicEditor topicEditor = new TopicEditor(this);
		editWindow = new DialogBase("Neues Thema", topicEditor);
		editWindow.setModal(true);
		editWindow.setWidth("600px");
		editWindow.setHeight("600px");
		editWindow.open();
	}


	public void saveTopic(String title, String description) {
		SurveyHelper.addTopic(survey, person, title, description);
		editWindow.close();
		init();
		
	}

	private void collectData() {
		surveyTopicWrappers = SurveyHelper.getTopicWrappers(survey, person);
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			SurveyTopic surveyTopic = new SurveyTopic();
			surveyTopic.setSurveyId(survey.getId());
			List<SurveyTopic> surveyTopics = dbContext.loadObjects(surveyTopic, false);
			if (surveyTopics != null) {
				for (SurveyTopic topic : surveyTopics) {
					SurveyVote surveyVote = new SurveyVote();
					surveyVote.setSurveyTopicId(topic.getId());
					
					// TODO
				}
			}
			

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		
	}

	private <T> List<T> loadAll(DBContext dbContext, Class<T> clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		T bean = Classes.newInstance(clazz.getName());
		return dbContext.loadObjects(bean, false);
	}

	
	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		title = new Span();
		description = new Span();
		buttonLayout = new HorizontalLayout();
		addTopicButton = new Button("Neues Thema");
		saveButton = new Button("Speichern");
		notifyCheckbox = new Checkbox("Benachrichtigen");
		buttonLayout.add(addTopicButton, saveButton, notifyCheckbox);
		
		table = new Grid<>();
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
