package org.mgnl.nicki.consulting.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.core.context.AppContext;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.dynamic.objects.objects.Template;
import org.mgnl.nicki.report.XlsDocuHelper;
import org.mgnl.nicki.template.engine.ConfigurationFactory.TYPE;
import org.mgnl.nicki.template.engine.TemplateEngine;
import org.mgnl.nicki.vaadin.base.menu.application.ConfigurableView;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.StreamResource;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportsView extends BaseView implements ConfigurableView  {
	
	private Grid<TimeWrapper> timeTable;
	
	private HorizontalLayout summaryLayout;
	
	private TextField daysLabel;
	
	private TextField hoursLabel;
	
	private VerticalLayout verticalLayout_2;
	
	private HorizontalLayout reportsLayout;
	
	private Anchor downloadPdfAnchor;
	
	private Anchor downloadXlsAnchor;
	
	private Select<Template> reportComboBox;
	
	private HorizontalLayout filterLayout;
	
	private ComboBox<Project> projectComboBox;
	
	private ComboBox<Customer> customerComboBox;
	
	private Select<PERIOD> timeComboBox;
	
	private Select<Person> personComboBox;

	private static final long serialVersionUID = -2330776406366438437L;
	private static final Logger LOG = LoggerFactory.getLogger(ReportsView.class);

	public static final String PROPERTY_BASE_DN = "nicki.templates.basedn";
	
	private enum TEMPLATE_TYPE {TEMPLATE, GROUP_OF_TEMPLATES, UNKNOWN}
	
	private boolean isInit;
	
	private Collection<Member> members;
	
	private ListDataProvider<TimeWrapper> timeDataProvider;
	Map<String, String> configuration;
	
	public ReportsView() {
		buildMainLayout();

	}


	@Override
	public void init() {
		if (!isInit) {
			try {
				initReportComboBox();
			} catch (InvalidPrincipalException e) {
				LOG.error("Error init reportComboBox", e);
			}

			reportComboBox.addValueChangeListener(event -> { timeComboBoxChanged(); });
			initTimeComboBox(this.timeComboBox);
			try {
				initPersonComboBox(this.personComboBox, ALL.TRUE);
			} catch (NoValidPersonException | NoApplicationContextException e) {
				LOG.error("Error init personComboBox", e);
			}
			personComboBox.addValueChangeListener(event -> { timeComboBoxChanged(); });
			customerComboBox.setEnabled(true);
			projectComboBox.setEnabled(true);

			downloadPdfAnchor.setEnabled(false);
			downloadXlsAnchor.setEnabled(false);
			

			timeTable.addColumn(TimeWrapper::getPersonName).setHeader("Person");
			timeTable.addComponentColumn(TimeWrapper::getMember).setHeader("Projekt").setWidth("300px");
			timeTable.addColumn(TimeWrapper::getDisplayDay).setHeader("Datum").setWidth("60px");
			timeTable.addColumn(TimeWrapper::getDisplayStart).setHeader("von").setWidth("40px");
			timeTable.addColumn(TimeWrapper::getDisplayEnd).setHeader("bis").setWidth("40px");
			timeTable.addColumn(TimeWrapper::getDisplayPause).setHeader("Pause").setWidth("60px");
			timeTable.addColumn(TimeWrapper::getHours).setHeader("Stunden").setWidth("40px");
			timeTable.addComponentColumn(TimeWrapper::getCustomerReport).setHeader(createIcon(VaadinIcon.FILE, "Bei Kunde erfasst"));
			timeTable.addColumn(TimeWrapper::getTextString).setHeader("T�tigkeit").setWidth("200px");
			
			timeComboBox.addValueChangeListener(event -> {timeComboBoxChanged();});
			customerComboBox.addValueChangeListener(event -> {customerComboBoxChanged();});
			projectComboBox.addValueChangeListener(event -> {projectComboBoxChanged();});
			projectComboBox.setItemLabelGenerator(Project::getName);

			timeComboBoxChanged();
			isInit = true;
		}
		try {
			initPersonData();
		} catch (NoValidPersonException | NoApplicationContextException  e1) {
			log.error("Error loading person data", e1);
		}
		initCustomerComboBox();
		initProjectComboBox();
		loadTimes();
	}

	private void initReportComboBox() throws InvalidPrincipalException {
		reportComboBox.setItems();
		reportComboBox.setItemLabelGenerator(Template::getName);
		if (getTemplateType() == TEMPLATE_TYPE.TEMPLATE)  {
			
			Template template = loadTemplate(getTemplatePath());
			reportComboBox.setItems(template);
			reportComboBox.setValue(template);
		} else if (getTemplateType() == TEMPLATE_TYPE.GROUP_OF_TEMPLATES) {

			List<Template> templates = loadTemplates(getTemplatePath());
			reportComboBox.setItems(templates);
			if (templates != null) {
				reportComboBox.setValue(templates.get(0));
			}
		}
		reportComboBox.setEmptySelectionAllowed(false);
	}


	private Template loadTemplate(String templatePath) throws InvalidPrincipalException {
		String parts[] = StringUtils.split(templatePath, "/");
		StringBuilder sb = new StringBuilder();
		sb.insert(0, getTemplateBaseDn());
		for (String part : parts) {
			sb.insert(0, ",");
			sb.insert(0, part).insert(0, "ou=");
		}
		return AppContext.getSystemContext().loadObject(Template.class, getTemplateDn(templatePath));
	}
	
	private String getTemplateDn(String templatePath) {
		String parts[] = StringUtils.split(templatePath, "/");
		StringBuilder sb = new StringBuilder();
		sb.insert(0, getTemplateBaseDn());
		for (String part : parts) {
			sb.insert(0, ",");
			sb.insert(0, part).insert(0, "ou=");
		}
		return sb.toString();
	}


	private String getTemplateBaseDn() {
		return Config.getString(PROPERTY_BASE_DN);
	}


	private List<Template> loadTemplates(String templatePath) throws InvalidPrincipalException {
		return AppContext.getSystemContext().loadChildObjects(Template.class, getTemplateDn(templatePath), null);
	}


	private String getTemplatePath(Template template) {
		return template.getSlashPath(getTemplateBaseDn());
	}


	private void timeComboBoxChanged() {
		setPersonComboBoxValue((Person) this.personComboBox.getValue());
		try {
			initPersonData();
		} catch (NoValidPersonException | NoApplicationContextException  e1) {
			log.error("Error loading person data", e1);
		}
		initCustomerComboBox();
		initProjectComboBox();
		loadTimes();
		generate();
	}

	private void customerComboBoxChanged() {
		if (customerComboBox.getValue() != null) {
			projectComboBox.setValue(null);
		}
		try {
			initPersonData();
		} catch (NoValidPersonException | NoApplicationContextException  e1) {
			log.error("Error loading person data", e1);
		}
		initProjectComboBox();
		loadTimes();
		generate();
	}

	private void projectComboBoxChanged() {
		loadTimes();
		generate();
	}
	
	private void generate() {
		StreamResource pdfSource = createPDFStream();
		downloadPdfAnchor.setEnabled(true);
		downloadPdfAnchor.setHref(pdfSource);
		downloadPdfAnchor.setText("Download PDF");

		StreamResource xlsSource = createXlsStream();
		downloadXlsAnchor.setHref(xlsSource);
		downloadXlsAnchor.setEnabled(true);
		downloadXlsAnchor.setText("Download XLS");
		
	}

	private void loadTimes() {
		try {
			PERIOD period = (PERIOD) timeComboBox.getValue();
			if (period != null) {

				timeDataProvider = new ListDataProvider<TimeWrapper>(getTimeWrappers(getPersonComboBoxValue(), period.getPeriod(), customerComboBox.getValue(), projectComboBox.getValue(), READONLY.TRUE, 0));
				timeTable.setDataProvider(timeDataProvider);
			}
		} catch (IllegalStateException | IllegalArgumentException | TimeSelectException e) {
			log.error("Error reading timd data", e);
		}
		float total = TimeHelper.getHoursFromTimeWrapperList(timeDataProvider.getItems());
		hoursLabel.setValue(String.format("%.2f", total));
		daysLabel.setValue(String.format("%.2f", total/8.0));
	}

	private Collection<TimeWrapper> getReportData() {
		return timeDataProvider.getItems();
	}

	private List<Time> getTimes() {
		List<Time> times = new ArrayList<>();
		if (timeDataProvider.getItems() != null) {
			for (TimeWrapper timeWrapper : timeDataProvider.getItems()) {
				times.add(timeWrapper.getTime());
			}
		}
		return times;
	}


	private void initCustomerComboBox() {
	

		customerComboBox.setItemLabelGenerator(Customer::getName);
		Collection<Customer> customers = getCustomers(members);
		if (customers != null && customers.size() > 0) {
			customerComboBox.setItems(customers);
		} else {
			customerComboBox.setItems();
		}
		customerComboBox.setValue(null);
	}

	private void initPersonData() throws NoValidPersonException, NoApplicationContextException {
		
		members = getMembers(getPersonComboBoxValue(), ((PERIOD) timeComboBox.getValue()).getPeriod());
		
	}


	private void initProjectComboBox() {

		Collection<Project> projects = getProjects((Customer) customerComboBox.getValue(), members);
		if (projects != null && projects.size() > 0) {
			projectComboBox.setItems(projects);
		} else {
			projectComboBox.setItems();
		}
		projectComboBox.setValue(null);
	}


	private StreamResource createPDFStream() {
		return new StreamResource("TimeReport_" + DataHelper.getMilli(new Date()) + ".pdf",
				() -> {
//				String template = "reports/timeReport/TimeReport";
				if (reportComboBox.getValue() == null) {
					Notification.show("Bitte Report w�hlen", Type.ERROR_MESSAGE);
				}
				Template template = reportComboBox.getValue();

				TemplateEngine engine = TemplateEngine.getInstance(TYPE.JNDI);
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("data", getReportData());
				params.put("times", getTimes());
				params.put("timeHelper", new TimeHelper());
				params.put("dataHelper", new DataHelper());
				params.put("today", DataHelper.getDisplayDay(new Date()));
				try {
//					return engine.executeTemplate(template + ".ftl", params, "UTF-8");
					return engine.executeTemplateAsPdf2(getTemplatePath(template) + ".ftl", params);
				} catch ( IOException | TemplateException | InvalidPrincipalException | ParserConfigurationException | SAXException | DocumentException  e) {
					LOG.error("Error generating Report", e);
				}
				return null;
			
		});
	}


	@SuppressWarnings("deprecation")
	private StreamResource createXlsStream() {
		return new StreamResource("TimeReport_" + DataHelper.getMilli(new Date()) + ".xls", () ->
			{
				if (reportComboBox.getValue() == null) {
					Notification.show("Bitte Report w�hlen", Type.ERROR_MESSAGE);
				}
				Template template = reportComboBox.getValue();

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("data", getReportData());
				params.put("times", getTimes());
				params.put("timeHelper", new TimeHelper());
				params.put("dataHelper", new DataHelper());
				params.put("today", DataHelper.getDisplayDay(new Date()));
				try {
					return XlsDocuHelper.generate(TYPE.JNDI, getTemplatePath(template) + ".xls", params);
				} catch ( IOException | TemplateException | InvalidPrincipalException | ParserConfigurationException | SAXException | DocumentException  e) {
					LOG.error("Error generating Report", e);
				}
				return null;
			
		});
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
		
		// summaryLayout
		summaryLayout = buildSummaryLayout();
		verticalLayout_2.add(summaryLayout);
		
		// timeTable
		timeTable = new Grid<>();
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
		verticalLayout_2.setPadding(false);
		
		// filterLayout
		filterLayout = buildFilterLayout();
		verticalLayout_2.add(filterLayout);
		
		// reportsLayout
		reportsLayout = buildReportsLayout();
		verticalLayout_2.add(reportsLayout);
		
		return verticalLayout_2;
	}


	
	private HorizontalLayout buildFilterLayout() {
		// common part: create layout
		filterLayout = new HorizontalLayout();
		filterLayout.setWidth("-1px");
		filterLayout.setHeight("-1px");
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		filterLayout.setPadding(false);
		
		// personComboBox
		personComboBox = new Select<>();
		personComboBox.setLabel("Person");
		personComboBox.setWidth("-1px");
		personComboBox.setHeight("-1px");
		filterLayout.add(personComboBox);
		
		// timeComboBox
		timeComboBox = new Select<>();
		timeComboBox.setLabel("Zeitraum");
		timeComboBox.setWidth("-1px");
		timeComboBox.setHeight("-1px");
		filterLayout.add(timeComboBox);
		
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


	
	private HorizontalLayout buildReportsLayout() {
		// common part: create layout
		reportsLayout = new HorizontalLayout();
		reportsLayout.setWidth("-1px");
		reportsLayout.setHeight("-1px");
		reportsLayout.setMargin(false);
		reportsLayout.setSpacing(true);
		reportsLayout.setPadding(false);
		reportsLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
		
		// reportComboBox
		reportComboBox = new Select<>();
		reportComboBox.setLabel("Report");
		reportComboBox.setWidth("-1px");
		reportComboBox.setHeight("-1px");
		reportsLayout.add(reportComboBox);
		
		// downloadPdfAnchor
		downloadPdfAnchor = new Anchor();
		downloadPdfAnchor.setText("PDF");
		downloadPdfAnchor.setWidth("-1px");
		downloadPdfAnchor.setHeight("-1px");
		reportsLayout.add(downloadPdfAnchor);
		
		// downloadXlsAnchor
		downloadXlsAnchor = new Anchor();
		downloadXlsAnchor.setText("Excel");
		downloadXlsAnchor.setWidth("-1px");
		downloadXlsAnchor.setHeight("-1px");
		reportsLayout.add(downloadXlsAnchor);
		
		return reportsLayout;
	}


	
	private HorizontalLayout buildSummaryLayout() {
		// common part: create layout
		summaryLayout = new HorizontalLayout();
		summaryLayout.setWidth("-1px");
		summaryLayout.setHeight("-1px");
		summaryLayout.setMargin(false);
		summaryLayout.setSpacing(true);
		summaryLayout.setPadding(false);
		
		// hoursLabel
		hoursLabel = new TextField();
		hoursLabel.setLabel("Summe (h)");
		hoursLabel.setReadOnly(true);
		hoursLabel.setWidth("-1px");
		hoursLabel.setHeight("-1px");
		hoursLabel.setValue("0");
		summaryLayout.add(hoursLabel);
		
		// daysLabel
		daysLabel = new TextField();
		daysLabel.setLabel("Summe (Tage)");
		daysLabel.setReadOnly(true);
		daysLabel.setWidth("-1px");
		daysLabel.setHeight("-1px");
		daysLabel.setValue("0");
		summaryLayout.add(daysLabel);
		
		return summaryLayout;
	}


}
