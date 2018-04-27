package org.mgnl.nicki.consulting.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.helper.InvoiceHelper;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Invoice;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.db.InvoiceTimeSelectHandler;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class InvoiceWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceWrapper.class);

	private Invoice invoice;
	private FileDownloader invoicePdfFileDownloader;
	private FileDownloader timeSheetPdfFileDownloader;
	
	public InvoiceWrapper(Invoice invoice) {
		this.setInvoice(invoice);
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}
	
	public Customer getCustomer() {
		return TimeHelper.getCustomer(getProject().getCustomerId());
	}
	
	public String getCustomerName() {
		return getCustomer().getName();
	}

	public Project getProject() {
		return TimeHelper.getProject(invoice.getProjectId());
	}
	
	public String getProjectName() {
		return getProject().getName();
	}
	
	public String getInvoiceNumber() {
		return invoice.getInvoiceNumber();
	}
	
	public Date getStart() {
		return invoice.getStart();
	}

	public Date getEnd() {
		return invoice.getEnd();
	}

	public Date getInvoiceDate() {
		return invoice.getInvoiceDate();
	}
	
	public Component getInvoiceDocument() {
		Button downloadButton  = new Button("Download");
		StreamResource pdfSource = createInvoicePDFStream();
		if (invoicePdfFileDownloader != null) {
			downloadButton.removeExtension(invoicePdfFileDownloader);
		}
		invoicePdfFileDownloader = new FileDownloader(pdfSource);
		invoicePdfFileDownloader.extend(downloadButton);
		downloadButton.setEnabled(true);
		downloadButton.setCaption("Download");

		return downloadButton;
	}

	private StreamResource createInvoicePDFStream() {
		return new StreamResource(
				() -> InvoiceHelper.renderInvoice(getProject(), getInvoiceParams()),
				"Invoice_" + DataHelper.getMilli(invoice.getInvoiceDate()) + ".pdf");
	}
	
	protected Map<String, Object> getInvoiceParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("times", getTimes());
		params.put("invoiceNumber", invoice.getInvoiceNumber());
		params.put("firstDay", invoice.getStart());
		params.put("lastDay", new Date(invoice.getEnd().getTime() - InvoiceHelper.DAY_IN_MS));
		params.put("timeHelper", new TimeHelper());
		params.put("dataHelper", new DataHelper());
		params.put("today", invoice.getInvoiceDate());
		return params;
	}
	
	public Component getTimeSheetDocument() {
		Button downloadButton  = new Button("Download");
		StreamResource pdfSource = createTimeSheetPDFStream();
		if (timeSheetPdfFileDownloader != null) {
			downloadButton.removeExtension(timeSheetPdfFileDownloader);
		}
		timeSheetPdfFileDownloader = new FileDownloader(pdfSource);
		timeSheetPdfFileDownloader.extend(downloadButton);
		downloadButton.setEnabled(true);
		downloadButton.setCaption("Download");

		return downloadButton;
	}

	private StreamResource createTimeSheetPDFStream() {
		return new StreamResource(
				() -> InvoiceHelper.renderTimeSheet(getProject(), getTimeSheetParams()),
				"TimeSheet_" + DataHelper.getMilli(invoice.getInvoiceDate()) + ".pdf");
	}
	
	protected Map<String, Object> getTimeSheetParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("times", getTimes());
		params.put("invoiceNumber", invoice.getInvoiceNumber());
		params.put("firstDay", invoice.getStart());
		params.put("lastDay", new Date(invoice.getEnd().getTime() - InvoiceHelper.DAY_IN_MS));
		params.put("timeHelper", new TimeHelper());
		params.put("dataHelper", new DataHelper());
		params.put("today", invoice.getInvoiceDate());
		return params;
	}

	private List<Time> getTimes() {

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			InvoiceTimeSelectHandler selectHandler = new InvoiceTimeSelectHandler(invoice.getId(), Constants.DB_CONTEXT_NAME);
			dbContext.select(selectHandler);
			return selectHandler.getList();
		} catch (SQLException | InitProfileException | TimeSelectException e) {
			LOG.error("Could not load invoice times", e);
		}
		return new ArrayList<>();
	}

}
