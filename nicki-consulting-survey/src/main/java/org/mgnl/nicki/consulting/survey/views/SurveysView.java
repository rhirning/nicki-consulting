package org.mgnl.nicki.consulting.survey.views;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.views.NoApplicationContextException;
import org.mgnl.nicki.consulting.views.NoValidPersonException;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.TextRenderer;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
public class SurveysView extends VerticalLayout implements View {

	Calendar today = Period.getTodayCalendar();
	Calendar tomorrow = Period.getTomorrowCalendar();
	private Select<SurveyConfig> surveysSelect;
	private VerticalLayout canvas;
	private boolean isInit;
	
	private List<SurveyConfig> surveys;
	private SurveyConfig survey;
	private @Setter @Getter NickiApplication application;
	private @Setter @Getter Map<String, String> configuration;
	private Person person;

	public SurveysView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			surveysSelect.setRenderer(new TextRenderer<>(SurveyConfig::getName));
			surveysSelect.setHeight("-1px");
			String columnWidth = configuration != null && configuration.get("columnWidth") != null ? configuration.get("columnWidth") : "400px";
			surveysSelect.setWidth(columnWidth);

			surveysSelect.addValueChangeListener(event -> {
				canvas.removeAll();
				SurveyConfig survey = event.getValue();
				if (survey != null) {
					try {
						showSurvey(survey);
					} catch (NoValidPersonException | NoApplicationContextException e) {
						Notification.show("Could not load Person");
					}
				}
			});
			
			isInit = true;
		}
		
		surveysSelect.setVisible(false);
//		setFlexGrow(0, table);
		setFlexGrow(1, canvas);
		canvas.removeAll();
		collectData();
		survey = null;
		if (surveys != null) {
			if (surveys.size() > 1) {
				surveysSelect.setItems(surveys);
				surveysSelect.setValue(surveys.get(0));
				surveysSelect.setVisible(true);
			} else if (surveys.size() == 1){
				survey = surveys.get(0);
				surveysSelect.setVisible(false);
				try {
					showSurvey(survey);
				} catch (NoValidPersonException | NoApplicationContextException e) {
					Notification.show("Could not load Person");
				}
			}
		}
	}

	private void showSurvey(SurveyConfig survey) throws NoValidPersonException, NoApplicationContextException {
		if (isFinished(survey)) {
			canvas.add(new FinishedSurveyView(survey, null, getPerson()));
		} else {
			canvas.add(new SurveyView(survey, getPerson()));
			
		}
	}

	private boolean isFinished(SurveyConfig survey) {
		if (survey.getEnd() == null) {
			return false;
		}
		return today.getTime().after(survey.getEnd());
	}

	private void collectData() {
		surveys = SurveyHelper.getAllActiveSurveys();
	}

	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		
		surveysSelect = new Select<>();
		
		canvas = new VerticalLayout();
		canvas.setSizeFull();
		canvas.setMargin(false);
		canvas.setSpacing(false);
		add(surveysSelect,canvas);
	}
	
	public Person getPerson() throws NoValidPersonException, NoApplicationContextException {
		if (person == null) {
			person = SurveyHelper.getActivePerson(application);
		}
		return person;
		
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}


}
