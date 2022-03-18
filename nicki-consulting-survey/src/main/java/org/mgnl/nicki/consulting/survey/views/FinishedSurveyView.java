package org.mgnl.nicki.consulting.survey.views;

import java.util.List;
import java.util.stream.Collectors;

import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.GridHelper;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyConfigWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

@SuppressWarnings("serial")
public class FinishedSurveyView extends VerticalLayout implements View {

	private Button backButton;
	private Span title;
	private Span description;
	private Grid<SurveyTopicWrapper> table;
	private boolean isInit;
	
	private List<SurveyTopicWrapper> surveyTopicWrappers;
	private SurveyConfigWrapper survey;
	private SurveyConfigView surveyConfigView;
	private Person person;


	public FinishedSurveyView(SurveyConfig survey, SurveyConfigView surveyConfigView, Person person) {
		this.survey = new SurveyConfigWrapper(survey);
		this.surveyConfigView = surveyConfigView;
		this.person = person;
		buildMainLayout();
		
		init();

	}

	public void init() {
		if (!isInit) {
			title.setText(survey.getName());
			description.setText(survey.getDescription());
			table.addColumn(SurveyTopicWrapper::getName).setHeader("Thema");
			table.addColumn(SurveyTopicWrapper::getValue).setHeader("Bewertung");;

			table.setItemDetailsRenderer(new ComponentRenderer<>(t -> {
				List<String> comments = SurveyHelper.getComments(t.getTopic());
				if (comments.size() == 0) {
					return new Span("Keine Kommentare");
				} else {
					Grid<String> commentsGrid = new Grid<>();
					commentsGrid.addColumn(s -> s.toString());
					commentsGrid.setItems(comments);
					commentsGrid.setAllRowsVisible(true);
					
					return commentsGrid;
				}
			}));
			GridHelper.addSummarySurveyTopicWrapperColumns(table, survey);
			table.setSizeFull();
			table.setAllRowsVisible(true);
			
			isInit = true;
		}
		collectData();
		if (surveyTopicWrappers != null) {
			table.setItems(surveyTopicWrappers);
		}
	}

	private void collectData() {
		surveyTopicWrappers = SurveyHelper.getTopicWrappers(survey.getSurveyConfig(), person)
				.stream().sorted((t1,t2) -> t2.getValue() - t1.getValue()).collect(Collectors.toList());
	}

	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		if (surveyConfigView != null) {
			backButton = new Button("Zurück");
			backButton.addClickListener(event -> surveyConfigView.init());
			add(backButton);
		}
		title = new Span();
		description = new Span();
		
		table = new Grid<>();
		add(title, description, table);
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
