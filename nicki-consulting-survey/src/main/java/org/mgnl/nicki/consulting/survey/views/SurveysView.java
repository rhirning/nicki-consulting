package org.mgnl.nicki.consulting.survey.views;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.views.NoApplicationContextException;
import org.mgnl.nicki.consulting.views.NoValidPersonException;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class SurveysView extends HorizontalLayout implements View {
	
	private Grid<SurveyConfig> surveysGrid;
	private VerticalLayout canvas;
	private boolean isInit;
	
	private List<SurveyConfig> surveys;
	private SurveyConfig survey;
	private DialogBase editWindow;

	private @Setter @Getter NickiApplication application;
	private @Setter @Getter Map<String, String> configuration;
	private Person person;

	public SurveysView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			surveysGrid.addColumn(SurveyConfig::getName).setWidth("-1px");
//			table.setItemDetailsRenderer(new TextRenderer<>(SurveyConfig::getDescription));

			surveysGrid.setHeightFull();
			String columnWidth = configuration != null && configuration.get("columnWidth") != null ? configuration.get("columnWidth") : "400px";
			surveysGrid.setWidth(columnWidth);
			surveysGrid.setAllRowsVisible(true);

			surveysGrid.addSelectionListener(event -> {
				canvas.removeAll();
				Optional<SurveyConfig> itemOptional = event.getFirstSelectedItem();
				if (itemOptional.isPresent()) {
					SurveyConfig survey = itemOptional.get();
					try {
						showSurvey(survey);
					} catch (NoValidPersonException | NoApplicationContextException e) {
						Notification.show("Could not load Person");
					}
				}
			});
			
			isInit = true;
		}
		
		surveysGrid.setVisible(false);
//		setFlexGrow(0, table);
//		setFlexGrow(1, canvas);
		canvas.removeAll();
		collectData();
		survey = null;
		if (surveys != null) {
			if (surveys.size() > 1) {
				surveysGrid.setItems(surveys);
				surveysGrid.setVisible(true);
			} else if (surveys.size() == 1){
				survey = surveys.get(0);
				surveysGrid.setVisible(false);
				try {
					showSurvey(survey);
				} catch (NoValidPersonException | NoApplicationContextException e) {
					Notification.show("Could not load Person");
				}
			}
		}
	}

	private void showSurvey(SurveyConfig survey) throws NoValidPersonException, NoApplicationContextException {
		SurveyView view = new SurveyView(survey, getPerson());
		canvas.add(view);
	}

	private <T> void showEditView(VerticalLayout layout, T object) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				init();
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidthFull();
		if (object != null) {
			beanViewer.setDbBean(object);
			layout.add(beanViewer);
		}
	}

	private void collectData() {
		surveys = SurveyHelper.getAllSurveys();
	}

	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		
		surveysGrid = new Grid<>();
		
		canvas = new VerticalLayout();
		canvas.setSizeFull();
		canvas.setMargin(false);
		canvas.setSpacing(false);
		add(surveysGrid,canvas);
	}
	
	public Person getPerson() throws NoValidPersonException, NoApplicationContextException {
		if (person == null) {
			loadPerson();
		}
		return person;
		
	}
	
	private void loadPerson() throws NoValidPersonException, NoApplicationContextException {

		if (application == null) {
			throw new NoApplicationContextException();
		}
		LdapPerson ldapPerson = (LdapPerson) application.getDoubleContext().getLoginContext().getUser();
		Person person = new Person();
		person.setUserId(ldapPerson.getName());

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			this.person = dbContext.loadObject(person, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load person", e);
			throw new NoValidPersonException(ldapPerson.getDisplayName());
		}
		
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}


}
