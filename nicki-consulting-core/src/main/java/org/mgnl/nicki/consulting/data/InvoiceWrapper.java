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
import org.mgnl.nicki.consulting.db.CheckForLatestInvoiceSelectHandler;
import org.mgnl.nicki.consulting.db.InvoiceTimeSelectHandler;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.consulting.db.UndoCommand;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.components.ConfirmDialog;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.StreamResource;

public class InvoiceWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceWrapper.class);

	private Invoice invoice;
	private Reloader reloader;
	private Button undoButton;
	
	public InvoiceWrapper(Invoice invoice, Reloader reloader) {
		this.setInvoice(invoice);
		this.reloader = reloader;
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
		Anchor anchor  = new Anchor();
		StreamResource pdfSource = createInvoicePDFStream();
		anchor.setEnabled(true);
		anchor.setHref(pdfSource);
		anchor.setText("Download");

		return new Div(anchor);
	}

	private StreamResource createInvoicePDFStream() {
		return new StreamResource("Invoice_" + DataHelper.getMilli(invoice.getInvoiceDate()) + ".pdf",
				() -> InvoiceHelper.renderInvoice(getProject(), getInvoiceParams()));
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
	
	public Component getUndoButton() {
		undoButton  = new Button("Storno");
		undoButton.addClickListener(event -> showUndo());
		return undoButton;
	}
	
	private void showUndo() {
		// check for latest project invoice
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			CheckForLatestInvoiceSelectHandler handler = new CheckForLatestInvoiceSelectHandler(invoice, Constants.DB_CONTEXT_NAME);
			dbContext.select(handler);
			if (!handler.isLatest()) {
				Notification.show("Es kann nur die letzte Rechnung eines Projekts storniert werden", Type.ERROR_MESSAGE);
				return;
			}
		} catch (SQLException | InitProfileException | TimeSelectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UndoCommand undoCommand = new UndoCommand(invoice, reloader);
		new ConfirmDialog(undoCommand).open();;
	}

	public Component getTimeSheetDocument() {
		Anchor anchor  = new Anchor();
		StreamResource pdfSource = createTimeSheetPDFStream();
		anchor.setEnabled(true);
		anchor.setHref(pdfSource);
		anchor.setText("Download");

		return new Div(anchor);
	}

	private StreamResource createTimeSheetPDFStream() {
		return new StreamResource("TimeSheet_" + DataHelper.getMilli(invoice.getInvoiceDate()) + ".pdf",
				() -> InvoiceHelper.renderTimeSheet(getProject(), getTimeSheetParams()));
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
