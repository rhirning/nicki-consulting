package org.mgnl.nicki.consulting.core.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.template.engine.TemplateEngine;
import org.mgnl.nicki.template.engine.ConfigurationFactory.TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;

import freemarker.template.TemplateException;

public class InvoiceHelper {
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceHelper.class);
	public static final String DEFAULT_INVOICE_TEMPLATE = "invoices/invoices/defaultInvoice";
	public static final String DEFAULT_TIMESHEET_TEMPLATE = "invoices/timesheet/defaultTimeSheet";
	
	public static final long SEC_IN_MS = 1000;
	public static final long MIN_IN_MS = 60 * SEC_IN_MS;
	public static final long HOUR_IN_MS = 60 * MIN_IN_MS;
	public static final long DAY_IN_MS = 24 * HOUR_IN_MS;


	public static String getInvoiceTemplate(Project project) {
		if (project.getInvoiceTemplate() != null) {
			return project.getInvoiceTemplate();
		}
		Customer customer = TimeHelper.getCustomer(project.getCustomerId());
		if (customer.getInvoiceTemplate() != null) {
			return customer.getInvoiceTemplate();
		}
		return DEFAULT_INVOICE_TEMPLATE;
	}
	
	public static InputStream renderInvoice(Project project, Map<String, Object> params) {
		TemplateEngine engine = TemplateEngine.getInstance(TYPE.JNDI);
		try {
			return engine.executeTemplateAsPdf2(getInvoiceTemplate(project) + ".ftl", params);
		} catch ( IOException | TemplateException | InvalidPrincipalException | ParserConfigurationException | SAXException | DocumentException  e) {
			LOG.error("Error generating Report", e);
		}
		return null;
	}

	public static String getTimeSheetTemplate(Project project) {
		if (project.getTimeSheetTemplate() != null) {
			return project.getTimeSheetTemplate();
		}
		Customer customer = TimeHelper.getCustomer(project.getCustomerId());
		if (customer.getTimeSheetTemplate() != null) {
			return customer.getTimeSheetTemplate();
		}
		return DEFAULT_TIMESHEET_TEMPLATE;
	}
	
	public static InputStream renderTimeSheet(Project project, Map<String, Object> params) {
		TemplateEngine engine = TemplateEngine.getInstance(TYPE.JNDI);
		try {
			return engine.executeTemplateAsPdf2(getTimeSheetTemplate(project) + ".ftl", params);
		} catch ( IOException | TemplateException | InvalidPrincipalException | ParserConfigurationException | SAXException | DocumentException  e) {
			LOG.error("Error generating Report", e);
		}
		return null;
	}
}
