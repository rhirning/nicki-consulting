package org.mgnl.nicki.consulting.views;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.data.BeanContainerDataSource;
import org.mgnl.nicki.consulting.data.InvoiceWrapper;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.vaadin.base.menu.application.ConfigurableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.FileDownloader;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class InvoicesView extends BaseView implements ConfigurableView  {
	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private Table timeTable;
	@AutoGenerated
	private VerticalLayout verticalLayout_2;
	@AutoGenerated
	private HorizontalLayout filterLayout;
	@AutoGenerated
	private ComboBox projectComboBox;
	@AutoGenerated
	private ComboBox customerComboBox;
	@AutoGenerated
	private ComboBox timeComboBox;


	private static final long serialVersionUID = -2330776406366438437L;
	private static final Logger LOG = LoggerFactory.getLogger(InvoicesView.class);

	public static final String PROPERTY_BASE_DN = "nicki.templates.basedn";
	
	private enum TEMPLATE_TYPE {TEMPLATE, GROUP_OF_TEMPLATES, UNKNOWN}
	
	private boolean isInit;

	private FileDownloader pdfFileDownloader;
	private BeanContainerDataSource<InvoiceWrapper> invoiceContainerDataSource;
	Map<String, String> configuration;
	
	public InvoicesView() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

	}


	@Override
	public void init() {
		if (!isInit) {
			initTimeComboBox(this.timeComboBox);
			customerComboBox.setEnabled(true);
			projectComboBox.setEnabled(true);
			
			invoiceContainerDataSource = new BeanContainerDataSource<>(InvoiceWrapper.class);
			timeTable.setContainerDataSource(invoiceContainerDataSource);
			timeTable.setVisibleColumns("customerName", "projectName", "start", "end", "invoiceDate", "invoiceNumber", "invoiceDocument", "timeSheetDocument");
			timeTable.setColumnHeaders("Kunde", "Projekt", "von", "bis", "Rechnungsdatum", "Rechnungsnummer", "Rechnung", "Zeitnachweis");
			
			timeComboBox.addValueChangeListener(event -> {timeComboBoxChanged();});
			customerComboBox.addValueChangeListener(event -> {customerComboBoxChanged();});
			projectComboBox.addValueChangeListener(event -> {projectComboBoxChanged();});
			timeComboBoxChanged();
			isInit = true;
		}
		initCustomerComboBox();
		initProjectComboBox();
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
		invoiceContainerDataSource.removeAllItems();
		try {
			PERIOD p = (PERIOD) timeComboBox.getValue();
			Period period = null;
			if (p != null) {
				period = p.getPeriod();
				invoiceContainerDataSource.addAll(getInvoiceWrappers(period, (Customer) customerComboBox.getValue(), (Project) projectComboBox.getValue()));
			}
		} catch (IllegalStateException | IllegalArgumentException | TimeSelectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initCustomerComboBox() {
	
		customerComboBox.removeAllItems();
		for (Customer customer : TimeHelper.getAllCustomers()) {
			customerComboBox.addItem(customer);
			customerComboBox.setItemCaption(customer, customer.getName());
		}
	}


	private void initProjectComboBox() {
	
		projectComboBox.removeAllItems();
		if (customerComboBox.getValue() != null) {
			for (Project project: TimeHelper.getProjects((Customer) customerComboBox.getValue())) {
				projectComboBox.addItem(project);
				projectComboBox.setItemCaption(project, project.getName());
			}
		}
	}

	public FileDownloader getPdfFileDownloader() {
		return pdfFileDownloader;
	}


	public void setPdfFileDownloader(FileDownloader pdfFileDownloader) {
		this.pdfFileDownloader = pdfFileDownloader;
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
	
	


	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// verticalLayout_2
		verticalLayout_2 = buildVerticalLayout_2();
		mainLayout.addComponent(verticalLayout_2);
		
		// timeTable
		timeTable = new Table();
		timeTable.setImmediate(false);
		timeTable.setWidth("-1px");
		timeTable.setHeight("100.0%");
		mainLayout.addComponent(timeTable);
		mainLayout.setExpandRatio(timeTable, 1.0f);
		
		return mainLayout;
	}


	@AutoGenerated
	private VerticalLayout buildVerticalLayout_2() {
		// common part: create layout
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setImmediate(false);
		verticalLayout_2.setWidth("-1px");
		verticalLayout_2.setHeight("-1px");
		verticalLayout_2.setMargin(true);
		verticalLayout_2.setSpacing(true);
		
		// filterLayout
		filterLayout = buildFilterLayout();
		verticalLayout_2.addComponent(filterLayout);
		
		
		return verticalLayout_2;
	}


	@AutoGenerated
	private HorizontalLayout buildFilterLayout() {
		// common part: create layout
		filterLayout = new HorizontalLayout();
		filterLayout.setImmediate(false);
		filterLayout.setWidth("-1px");
		filterLayout.setHeight("-1px");
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		
		// timeComboBox
		timeComboBox = new ComboBox();
		timeComboBox.setCaption("Zeitraum");
		timeComboBox.setImmediate(true);
		timeComboBox.setWidth("-1px");
		timeComboBox.setHeight("-1px");
		filterLayout.addComponent(timeComboBox);
		
		// customerComboBox
		customerComboBox = new ComboBox();
		customerComboBox.setCaption("Kunde");
		customerComboBox.setImmediate(true);
		customerComboBox.setWidth("-1px");
		customerComboBox.setHeight("-1px");
		filterLayout.addComponent(customerComboBox);
		
		// projectComboBox
		projectComboBox = new ComboBox();
		projectComboBox.setCaption("Projekt");
		projectComboBox.setImmediate(true);
		projectComboBox.setWidth("-1px");
		projectComboBox.setHeight("-1px");
		filterLayout.addComponent(projectComboBox);
		
		return filterLayout;
	}


}
