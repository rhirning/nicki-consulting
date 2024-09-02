package org.mgnl.nicki.consulting.survey.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.GridHelper;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyConfigWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.template.engine.ConfigurationFactory.TYPE;
import org.mgnl.nicki.template.report.helper.XlsDocuHelper;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.xml.sax.SAXException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Slf4j
public class FinishedSurveyView extends VerticalLayout implements View {

	private static final String SURVEY_TEMPLATE = "survey/survey";
	private Button backButton;
	private Span title;
	private Span description;
	private Anchor downloadAnchor;
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
					return getDetails(comments);
				}
			}));
			GridHelper.addSummarySurveyTopicWrapperColumns(table, survey);
			table.setSizeFull();
			table.setAllRowsVisible(true);
			
			StreamResource xlsxSource = createXlsxStream();
			downloadAnchor.setEnabled(true);
			downloadAnchor.setHref(xlsxSource);
			
			isInit = true;
		}
		collectData();
		if (surveyTopicWrappers != null) {
			table.setItems(surveyTopicWrappers);
		}
	}


	private Component getDetails(List<String> comments) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		for (String comment : comments) {
			sb.append("<tr><td>").append(comment).append("</td>");
		}
		sb.append("</table>");
		return new Html(sb.toString());
	}

	private StreamResource createXlsxStream() {
		return new StreamResource("Umfrage_" + correct(survey.getName()) + "_" + DataHelper.getMilli(new Date()) + ".xlsx",
					() -> {
						try {
							return renderSurvey();
						} catch (IOException | TemplateException | InvalidPrincipalException
								| ParserConfigurationException | SAXException e) {
							log.error("Error rendering survey", e);
						}
						return null;
					});
	}


	
	private String correct(String name) {
		name = StringUtils.replace(name, "/", "_");
		name = StringUtils.replace(name, "\\", "_");
		name = StringUtils.replace(name, ":", "_");
		return name;
	}

	public InputStream renderSurvey() throws IOException, TemplateException, InvalidPrincipalException, ParserConfigurationException, SAXException {
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("survey", survey.getSurveyConfig());
		dataModel.put("helper", new SurveyHelper());
		dataModel.put("person", person);
		dataModel.put("topicWrappers", surveyTopicWrappers);
		dataModel.put("dataHelper", new DataHelper());
		return XlsDocuHelper.generateXlsx(TYPE.CLASSPATH, SURVEY_TEMPLATE, dataModel);

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
		downloadAnchor = new Anchor();
		downloadAnchor.setText("download");
		
		table = new Grid<>();
		table.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		add(title, description, downloadAnchor, table);
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
