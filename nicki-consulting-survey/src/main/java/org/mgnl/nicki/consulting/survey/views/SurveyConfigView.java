package org.mgnl.nicki.consulting.survey.views;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.core.util.Classes;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class SurveyConfigView extends VerticalLayout implements View {
	
	private SplitLayout horizontalSplitPanel;
	
	private Grid<SurveyConfig> table;
	private VerticalLayout tableCanvas;
	private VerticalLayout canvas;
	private boolean isInit;
	
	private List<SurveyConfig> surveys;
	private DialogBase editWindow;
	private @Setter @Getter NickiApplication application;
	private @Setter @Getter Map<String, String> configuration;


	public SurveyConfigView() {
		buildMainLayout();
	}

	public void init() {
		if (!isInit) {
			
			isInit = true;
		}
		
		tableCanvas.removeAll();
		canvas.removeAll();
		
		table = new Grid<>();
		table.addColumn(SurveyConfig::getName);
		table.setSizeFull();

		collectData();
		table.setItems(surveys);
		table.setAllRowsVisible(true);

		table.addSelectionListener(event -> {
			canvas.removeAll();
			Optional<SurveyConfig> itemOptional = event.getFirstSelectedItem();
			if (itemOptional.isPresent()) {
				SurveyConfig target = itemOptional.get();
				showEditView(canvas, target);
			}
		});

		// newSurveyButton
		Button newSurveyButton = new Button();
		newSurveyButton.setText("Neue Umfrage");
		newSurveyButton.setWidth("-1px");
		newSurveyButton.setHeight("-1px");
		newSurveyButton.addClickListener(event -> showEditView(SurveyConfig.class, Optional.empty(), "Neue Umfrage", "Umfrage bearbeiten"));
		tableCanvas.add(newSurveyButton);
		
		tableCanvas.add(table);
		tableCanvas.setFlexGrow(1, table);

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

	private void deleteSurvey(SurveyConfig survey ) {
		try {
			if (hasTopics(survey)) {
				Notification.show("Die Umfrage hat schon begonnen", Type.HUMANIZED_MESSAGE);
			} else {
				try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
					dbContext.delete(survey);
				}
				init();
			}
		} catch (SQLException | InitProfileException e) {
			log.error("Error accessing db", e);
			Notification.show("Die Umfrage konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
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
	
	private <T> void showEditView(Class<T> clazz, Optional<T> object, String newCaption, String editCaption, Object... foreignObjects) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				editWindow.close();
				init();
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidth("400px");
		String windowTitle;
		if (!object.isPresent()) {
			try {
				beanViewer.init(clazz, foreignObjects);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			windowTitle = newCaption;
		} else {
			beanViewer.setDbBean(object.get());
			windowTitle = editCaption;
		}
		editWindow = new DialogBase(windowTitle, beanViewer);
		editWindow.setModal(true);
		editWindow.setHeightFull();
		editWindow.open();
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
		
		// horizontalSplitPanel
		horizontalSplitPanel = new SplitLayout();
		horizontalSplitPanel.setOrientation(Orientation.HORIZONTAL);
		horizontalSplitPanel.setSizeFull();
		add(horizontalSplitPanel);
		
		tableCanvas = new VerticalLayout();
		tableCanvas.setSizeFull();
		tableCanvas.setMargin(false);
		tableCanvas.setSpacing(true);
		horizontalSplitPanel.addToPrimary(tableCanvas);
		
		canvas = new VerticalLayout();
		canvas.setSizeFull();
		canvas.setMargin(false);
		canvas.setSpacing(false);
		horizontalSplitPanel.addToSecondary(canvas);
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}
}
