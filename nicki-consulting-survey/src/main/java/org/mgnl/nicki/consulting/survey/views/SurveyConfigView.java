package org.mgnl.nicki.consulting.survey.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.GridHelper;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.consulting.views.NoApplicationContextException;
import org.mgnl.nicki.consulting.views.NoValidPersonException;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.core.helper.NameValue;
import org.mgnl.nicki.core.util.Classes;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class SurveyConfigView extends VerticalLayout implements View {
	
	private HorizontalLayout buttonLayout;
	private Button newSurveyButton;
	private Button editSurveyButton;
	private Button showResultButton;
	private Button deleteSurveyButton;
	private Grid<SurveyConfig> table;
	private VerticalLayout canvas;
	private boolean isInit;
	
	private List<SurveyConfig> surveys;
	private @Setter @Getter NickiApplication application;
	private @Setter @Getter Map<String, String> configuration;
	private Person person;


	public SurveyConfigView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			table.addColumn(SurveyConfig::getName);
			table.setItemDetailsRenderer(new ComponentRenderer<>(survey -> getDetails(survey)));
			table.setAllRowsVisible(true);

			table.addSelectionListener(event -> {
				Optional<SurveyConfig> itemOptional = event.getFirstSelectedItem();
				if (itemOptional.isPresent()) {
					editSurveyButton.setVisible(true);
					showResultButton.setVisible(true);
					deleteSurveyButton.setVisible(true);
				} else {
					editSurveyButton.setVisible(false);
					showResultButton.setVisible(false);
					deleteSurveyButton.setVisible(false);
				}
			});
			isInit = true;
		}
		
		collectData();
		buttonLayout.setVisible(true);
		table.setItems(surveys);
		table.setVisible(true);
		canvas.removeAll();
		canvas.setVisible(false);


		newSurveyButton.addClickListener(event -> showEditView(Optional.empty()));
		editSurveyButton.addClickListener(event -> {
			Optional<SurveyConfig> surveyOptional = Optional.ofNullable(table.getSelectedItems().iterator().next());
			showEditView(surveyOptional);
		});
		showResultButton.addClickListener(event -> {
			Optional<SurveyConfig> surveyOptional = Optional.ofNullable(table.getSelectedItems().iterator().next());
			showResultView(surveyOptional);
		});
		
		deleteSurveyButton.addClickListener(event -> {
			Optional<SurveyConfig> surveyOptional = Optional.ofNullable(table.getSelectedItems().iterator().next());
			
			if (surveyOptional.isPresent()) {
				SurveyConfig survey = surveyOptional.get();
				if (SurveyHelper.isDeleteAllowed(survey))
					try {
						SurveyHelper.delete(survey);
					} catch (SQLException | InitProfileException | NotSupportedException e) {
						log.error("Could not delete survey: " + survey, e);
					}
			} else {
				Notification.show("Da wurde schon abgestimmt");
			}
		});


        GridContextMenu<SurveyConfig> contextMenu = new GridContextMenu<>(table);
        // handle item right-click
        contextMenu.setDynamicContentHandler(item -> {
        	contextMenu.removeAll();
            if (item != null) {
            	table.select(item);
            	SurveyConfig target = item;
        		contextMenu.addItem("Löschen", selectedItem -> deleteSurvey(target));
            	return true;
            } else {
            	return false;
            }
        });
		
	}
	
	private Grid<NameValue> getDetails(SurveyConfig survey) {
		List<NameValue> values = new ArrayList<>();
		values.add(new NameValue("Beschreibung", survey.getDescription()));
		values.add(new NameValue("Kommentar erlaubt", survey.getAddComment() ? "Ja" : "Nein"));
		values.add(new NameValue("Themen hinzufügen erlaubt", survey.getAddTopic() ? "Ja" : "Nein"));
		if (survey.getStart() != null) {
			values.add(new NameValue("Start", DataHelper.getDisplayDay(survey.getStart())));
		}
		if (survey.getEnd() != null) {
			values.add(new NameValue("Ende", DataHelper.getDisplayDay(survey.getEnd())));
		}
		if (survey.getVisible() != null) {
			values.add(new NameValue("Sichtbar bis", DataHelper.getDisplayDay(survey.getVisible())));
		}
		return GridHelper.getDetails(values);
	}

	private void deleteSurvey(SurveyConfig survey ) {
		try {
			if (hasTopics(survey)) {
				Notification.show("Die Umfrage hat schon begonnen");
			} else {
				SurveyHelper.confirm("Umfrage löschen", survey, s -> {
					try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
						SurveyHelper.deleteTopics(s);
						SurveyHelper.deleteChoices(s);
						SurveyHelper.deleteNotifies(s);
						dbContext.delete(s);
					} catch (SQLException | InitProfileException e) {
						log.error("Error accessing db", e);
						Notification.show("Die Umfrage konnte nicht gelöscht werden");
					}
					init();
				});
			}
		} catch (SQLException | InitProfileException e) {
			log.error("Error accessing db", e);
			Notification.show("Die Umfrage konnte nicht gelöscht werden");
		}
	}

	private boolean hasTopics(SurveyConfig survey) throws SQLException, InitProfileException {
		SurveyTopic topic = new SurveyTopic();
		topic.setSurveyId(survey.getId());
		return isExist(topic);
	}

	public static boolean isExist(Object bean) throws SQLException, InitProfileException {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.exists(bean);
		}
	}
	
	private void showEditView(Optional<SurveyConfig> surveyOptional) {
		try {
			SurveyEditor surveyEditor = new SurveyEditor(getPerson(), this, surveyOptional);
			buttonLayout.setVisible(false);
			canvas.removeAll();
			canvas.add(surveyEditor);
			table.setVisible(false);
			canvas.setVisible(true);
		} catch (NoValidPersonException | NoApplicationContextException e) {
			Notification.show("Wer ist denn da angemeldet?");
		}
	}
	
	private void showResultView(Optional<SurveyConfig> surveyOptional) {
		try {
			FinishedSurveyView surveyEditor = new FinishedSurveyView(surveyOptional.get(), this, getPerson());
			buttonLayout.setVisible(false);
			canvas.removeAll();
			canvas.add(surveyEditor);
			table.setVisible(false);
			canvas.setVisible(true);
		} catch (NoValidPersonException | NoApplicationContextException e) {
			Notification.show("Wer ist denn da angemeldet?");
		}
	}
	
	public Person getPerson() throws NoValidPersonException, NoApplicationContextException {
		if (person == null) {
			person = SurveyHelper.getActivePerson(application);
		}
		return person;
		
	}

	private void collectData() {
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			surveys = loadAll(dbContext, SurveyConfig.class);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException | ClassNotFoundException e) {
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

		// buttonLayout
		buttonLayout = new HorizontalLayout();
		// newSurveyButton
		newSurveyButton = new Button("Neue Umfrage");
		// editSurveyButton
		editSurveyButton = new Button("Umfrage ändern");
		editSurveyButton.setVisible(false);
		
		showResultButton = new Button("Ergebnis");
		showResultButton.setVisible(false);
		
		deleteSurveyButton = new Button("Umfrage löschen");
		deleteSurveyButton.setVisible(false);
		
		buttonLayout.add(newSurveyButton, editSurveyButton, showResultButton, deleteSurveyButton);
		

		table = new Grid<>();
		table.setSizeFull();
		
		canvas = new VerticalLayout();
		canvas.setSizeFull();
		canvas.setVisible(false);

		
		add(buttonLayout, table, canvas);

	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}
}
