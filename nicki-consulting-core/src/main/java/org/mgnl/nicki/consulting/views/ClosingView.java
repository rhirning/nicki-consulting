package org.mgnl.nicki.consulting.views;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.db.OpenProject;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.ConfigurableView;
import org.mgnl.nicki.vaadin.base.notification.Notification;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

public class ClosingView extends BaseView implements ConfigurableView  {
	
	private HorizontalLayout horizontalLayout_1;
	
	private VerticalLayout verticalLayout_2;
	
	private Button closingButton;
	
	private Button previewInvoiceButton;
	
	private Grid<OpenProject> projectsTable;

	private static final long serialVersionUID = -2330776406366438437L;

	public static final String PROPERTY_BASE_DN = "nicki.templates.basedn";
	
	private boolean isInit;
	
	private Map<String, String> configuration;
	private DialogBase performClosingWindow;
	
	public ClosingView() {
		buildMainLayout();

	}


	@Override
	public void init() {
		if (!isInit) {
			
			projectsTable.addColumn(OpenProject::getCustomerName).setHeader("Kunde");
			projectsTable.addColumn(OpenProject::getProjectName).setHeader("Projekt");
			projectsTable.addColumn(OpenProject::getHours).setHeader("Stunden");
			projectsTable.addColumn(OpenProject::getDays).setHeader("Tage");
			projectsTable.addColumn(OpenProject::getSince).setHeader("offene Bunchungen seit");
			projectsTable.setSelectionMode(SelectionMode.SINGLE);
			
			previewInvoiceButton.setEnabled(false);
			closingButton.setEnabled(false);
			
			projectsTable.addSelectionListener(event -> projectSelected(event.getFirstSelectedItem()));
			previewInvoiceButton.addClickListener(event -> previewInvoice());
			closingButton.addClickListener(event -> performClosing());
			isInit = true;
		}
		loadProjects();
	}

	private void performClosing() {
		showPerformClosingWindow(true);
	}


	private void showPerformClosingWindow(boolean close) {
		OpenProject openProject = projectsTable.asSingleSelect().getValue();
		if (openProject != null && StringUtils.isNotBlank(openProject.getSince())) {
			PerformClosingView performClosingView = new PerformClosingView(this, openProject.getProject(), close);
			performClosingWindow = new DialogBase(close? "Abschluss" : "Vorschau", performClosingView);
			performClosingWindow.setModal(true);
			performClosingWindow.open();
		} else {
			Notification.show("Bitte ein gültiges Projekt wählen");
		}
	}
	
	public void finshClosing() {
		if (performClosingWindow != null) {
			performClosingWindow.close();
		}
		loadProjects();
	}


	private void previewInvoice() {
		showPerformClosingWindow(false);
	}


	private void projectSelected(Optional<OpenProject> selectedItem) {
		if (selectedItem.isPresent() && StringUtils.isNotBlank(selectedItem.get().getSince())) {
			previewInvoiceButton.setEnabled(true);
			closingButton.setEnabled(true);
		} else {
			previewInvoiceButton.setEnabled(false);
			closingButton.setEnabled(false);
		}
	}


	private void loadProjects() {
		
		ListDataProvider<OpenProject> dataProvider = new ListDataProvider<>(TimeHelper.getOpenProjects(null));
		projectsTable.setItems(dataProvider);
	}


	public Map<String, String> getConfiguration() {
		return configuration;
	}


	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}
	
	


	
	private void buildMainLayout() {
		setSizeFull();
		
		// horizontalLayout_1
		horizontalLayout_1 = buildHorizontalLayout_1();
		add(horizontalLayout_1);
	}


	
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		horizontalLayout_1 = new HorizontalLayout();
		horizontalLayout_1.setWidthFull();
		horizontalLayout_1.setHeightFull();
		horizontalLayout_1.setMargin(false);
		horizontalLayout_1.setSpacing(true);
		
		// projectsTable
		projectsTable = new Grid<OpenProject>();
		projectsTable.setWidthFull();
		projectsTable.setHeightFull();
		horizontalLayout_1.add(projectsTable);
		horizontalLayout_1.setFlexGrow(1, projectsTable);
		
		// verticalLayout_2
		verticalLayout_2 = buildVerticalLayout_2();
		horizontalLayout_1.add(verticalLayout_2);
		
		return horizontalLayout_1;
	}


	
	private VerticalLayout buildVerticalLayout_2() {
		// common part: create layout
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setWidth("-1px");
		verticalLayout_2.setHeight("-1px");
		verticalLayout_2.setMargin(true);
		verticalLayout_2.setSpacing(true);
		
		// previewInvoiceButton
		previewInvoiceButton = new Button();
		previewInvoiceButton.setText("Vorschau Rechnung");
		previewInvoiceButton.setWidth("200px");
		previewInvoiceButton.setHeight("-1px");
		verticalLayout_2.add(previewInvoiceButton);
		
		// closingButton
		closingButton = new Button();
		closingButton.setText("Monatsabschluss");
		closingButton.setWidth("200px");
		closingButton.setHeight("-1px");
		verticalLayout_2.add(closingButton);
		
		return verticalLayout_2;
	}

}
