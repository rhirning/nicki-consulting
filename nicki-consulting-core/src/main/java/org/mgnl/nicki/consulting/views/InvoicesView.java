package org.mgnl.nicki.consulting.views;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.data.InvoiceWrapper;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.vaadin.base.components.PeriodSelect;
import org.mgnl.nicki.vaadin.base.menu.application.ConfigurableView;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class InvoicesView extends BaseView implements ConfigurableView  {
	
	private Grid<InvoiceWrapper> timeTable;
	
	private VerticalLayout verticalLayout_2;
	
	private HorizontalLayout filterLayout;
	
	private ComboBox<Project> projectComboBox;
	
	private ComboBox<Customer> customerComboBox;
	
	private PeriodSelect periodSelect;


	private static final long serialVersionUID = -2330776406366438437L;
	public static final String PROPERTY_BASE_DN = "nicki.templates.basedn";
	
	private enum TEMPLATE_TYPE {TEMPLATE, GROUP_OF_TEMPLATES, UNKNOWN}
	
	private boolean isInit;

	Map<String, String> configuration;
	
	public InvoicesView() {
		buildMainLayout();

	}


	@Override
	public void init() {
		if (!isInit) {
			initPeriodSelect(this.periodSelect);
			customerComboBox.setEnabled(true);
			projectComboBox.setEnabled(true);
			
			timeTable.addColumn(InvoiceWrapper::getCustomerName).setHeader("Kunde");
			timeTable.addColumn(InvoiceWrapper::getProjectName).setHeader("Projekt");
			timeTable.addColumn(i -> DataHelper.getDisplayDay(i.getStart())).setHeader("von");
			timeTable.addColumn(i -> DataHelper.getDisplayDay(i.getEnd())).setHeader("bis");
			timeTable.addColumn(i -> DataHelper.getDisplayDay(i.getInvoiceDate())).setHeader("Rechnungsdatum");
			timeTable.addColumn(InvoiceWrapper::getInvoiceNumber).setHeader("Rechnungsnummer");
			timeTable.addComponentColumn(InvoiceWrapper::getInvoiceDocument).setHeader("Rechnung");
			timeTable.addComponentColumn(InvoiceWrapper::getTimeSheetDocument).setHeader("Zeitnachweis");
			timeTable.addComponentColumn(InvoiceWrapper::getUndoButton).setHeader("Storno");
			
			periodSelect.setConsumer(event -> timeComboBoxChanged());
			customerComboBox.addValueChangeListener(event -> {customerComboBoxChanged();});
			projectComboBox.addValueChangeListener(event -> {projectComboBoxChanged();});
			timeComboBoxChanged();
			isInit = true;
		}
		initCustomerComboBox();
		initProjectComboBox();
		loadInvoices();
	}
	
	@Override
	protected void reload() {
		loadInvoices();
	}


	private void timeComboBoxChanged() {
		initCustomerComboBox();
		initProjectComboBox();
		loadInvoices();
	}

	private void customerComboBoxChanged() {
		if (customerComboBox.getValue() != null) {
			projectComboBox.setValue(null);
		}
		initProjectComboBox();
		loadInvoices();
	}

	private void projectComboBoxChanged() {
		loadInvoices();
	}

	private void loadInvoices() {
		try {
			Period period = periodSelect.getValue();
			if (period != null) {
				timeTable.setItems(getInvoiceWrappers(period, (Customer) customerComboBox.getValue(), (Project) projectComboBox.getValue()));
			}
		} catch (IllegalStateException | IllegalArgumentException | TimeSelectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initCustomerComboBox() {
	
		customerComboBox.setItems(TimeHelper.getAllCustomers());
		customerComboBox.setItemLabelGenerator(Customer::getName);
	}


	private void initProjectComboBox() {
	
		projectComboBox.setItems();
		if (customerComboBox.getValue() != null) {
			projectComboBox.setItems(TimeHelper.getProjects((Customer) customerComboBox.getValue()));
			projectComboBox.setItemLabelGenerator(Project::getName);
		}
	}


	public Map<String, String> getConfiguration() {
		return configuration;
	}


	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}
	
	protected TEMPLATE_TYPE getTemplateType() {
		if (getConfiguration() != null) {
			for(TEMPLATE_TYPE type : TEMPLATE_TYPE.values()) {
				if (StringUtils.equals(type.name(), getConfiguration().get("type"))) {
					return type;
				}
			}
		}
		return TEMPLATE_TYPE.UNKNOWN;
	}
	
	protected String getTemplatePath() {
		if (getConfiguration() != null && getConfiguration().get("path") != null) {
			return getConfiguration().get("path");
		}
		return Config.getString("nicki.consulting.report.default", "reports/timeReport/TimeReport");
	}
	
	


	
	private void buildMainLayout() {
		setWidth("100%");
		setHeight("100%");
		
		// verticalLayout_2
		verticalLayout_2 = buildVerticalLayout_2();
		add(verticalLayout_2);
		
		// timeTable
		timeTable = new Grid<InvoiceWrapper>();
		timeTable.setWidth("100%");
		timeTable.setHeight("100.0%");
		verticalLayout_2.add(timeTable);
		verticalLayout_2.setFlexGrow(1, timeTable);
	}


	
	private VerticalLayout buildVerticalLayout_2() {
		// common part: create layout
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setSizeFull();
		verticalLayout_2.setMargin(true);
		verticalLayout_2.setSpacing(true);
		
		// filterLayout
		filterLayout = buildFilterLayout();
		verticalLayout_2.add(filterLayout);
		
		
		return verticalLayout_2;
	}


	
	private HorizontalLayout buildFilterLayout() {
		// common part: create layout
		filterLayout = new HorizontalLayout();
		filterLayout.setWidth("-1px");
		filterLayout.setHeight("-1px");
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		
		// periodSelect
		periodSelect = new PeriodSelect();
		filterLayout.add(periodSelect);
		
		// customerComboBox
		customerComboBox = new ComboBox<>();
		customerComboBox.setLabel("Kunde");
		customerComboBox.setWidth("-1px");
		customerComboBox.setHeight("-1px");
		customerComboBox.setClearButtonVisible(true);
		filterLayout.add(customerComboBox);
		
		// projectComboBox
		projectComboBox = new ComboBox<>();
		projectComboBox.setLabel("Projekt");
		projectComboBox.setWidth("-1px");
		projectComboBox.setHeight("-1px");
		projectComboBox.setClearButtonVisible(true);
		filterLayout.add(projectComboBox);
		
		return filterLayout;
	}


}
