package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.helper.InvoiceHelper;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Invoice;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.db.OpenTimeSelectHandler;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.context.PrimaryKey;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.data.DateHelper;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;


public class PerformClosingView extends VerticalLayout {
	private static final long serialVersionUID = -5801707593646769677L;
	
	private HorizontalLayout horizontalLayout_1;
	
	private Anchor downloadInvoiceAnchor;
	
	private Anchor downloadTimeSheetAnchor;
	
	private Button closeProjectButton;
	
	private DatePicker nextStart;
	
	private TextField invoiceNumberTextField;
	private Project project;
	private static final Logger LOG = LoggerFactory.getLogger(PerformClosingView.class);
	
	private ClosingView closingView;

	public PerformClosingView(ClosingView closingView, Project project, boolean closeProject) {
		this.closingView =closingView;
		this.project = project;
		buildMainLayout();
		downloadInvoiceAnchor.setEnabled(false);
		downloadTimeSheetAnchor.setEnabled(false);
		if (!closeProject) {
			closeProjectButton.setVisible(false);
		}
		closeProjectButton.setEnabled(false);

		closeProjectButton.addClickListener(event -> close());
		invoiceNumberTextField.addValueChangeListener(event -> valuesChanged());
		nextStart.addValueChangeListener(event -> valuesChanged());
	}
	
	private void valuesChanged() {
		downloadInvoiceAnchor.setEnabled(false);
		downloadTimeSheetAnchor.setEnabled(false);
		if (invoiceNumberTextField.getValue() != null && nextStart.getValue() != null) {
			generate();
			closeProjectButton.setEnabled(true);
		}
	}

	private void generate() {
		StreamResource invoicePdfSource = createInvoicePDFStream();
		downloadInvoiceAnchor.setHref(invoicePdfSource);
		downloadInvoiceAnchor.setEnabled(true);
		

		StreamResource timeSheetPdfSource = createTimeSheetPDFStream();
		downloadTimeSheetAnchor.setHref(timeSheetPdfSource);
		downloadTimeSheetAnchor.setEnabled(true);
		
	}

	private StreamResource createInvoicePDFStream() {
		return new StreamResource("Invoice_" + DataHelper.getMilli(new Date()) + ".pdf", 
				() -> InvoiceHelper.renderInvoice(this.project, getInvoiceParams()));
	}

	private StreamResource createTimeSheetPDFStream() {
		return new StreamResource("TimeSheet_" + DataHelper.getMilli(new Date()) + ".pdf",
				() -> InvoiceHelper.renderTimeSheet(this.project, getInvoiceParams()));
	}
	
	protected Map<String, Object> getInvoiceParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		List<Time> times = getTimes();
		if (times == null || times.size() == 0) {
			LOG.error("No data");
		}
		params.put("times", times);
		params.put("invoiceNumber", invoiceNumberTextField.getValue());
		params.put("firstDay", this.project.getOpen());
		params.put("lastDay", new Date(DataHelper.getDate(nextStart.getValue()).getTime() - InvoiceHelper.DAY_IN_MS));
		params.put("timeHelper", new TimeHelper());
		params.put("dataHelper", new DataHelper());
		params.put("today", DataHelper.getDisplayDay(new Date()));
		return params;
	}

	private List<Time> getTimes() {

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			OpenTimeSelectHandler selectHandler = new OpenTimeSelectHandler(this.project, this.project.getOpen(), DataHelper.getDate(nextStart.getValue()), Constants.DB_CONTEXT_NAME);
			dbContext.select(selectHandler);
			if (selectHandler.getList() != null) {
				return selectHandler.getList();
			};
		} catch (SQLException | InitProfileException | TimeSelectException e) {
			LOG.error("Could not load open times", e);
		}
		return new ArrayList<>();
	}

	private void close() {
		Invoice invoice = new Invoice();
		
		Calendar start = Calendar.getInstance();
		start.setTime(this.project.getOpen());
		Period.setToBeginOfDay(start);
		invoice.setStart(start.getTime());
		
		Calendar end = Calendar.getInstance();
		end.setTime(DataHelper.getDate(nextStart.getValue()));
		Period.setToBeginOfDay(end);
		invoice.setEnd(end.getTime());
		
		invoice.setInvoiceNumber(invoiceNumberTextField.getValue());
		invoice.setProjectId(this.project.getId());
		invoice.setInvoiceDate(new Date());
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			// create invoice entry -> invoiceId
			PrimaryKey primaryKey = dbContext.create(invoice);
			long invoiceId = primaryKey.getLong("ID");
			// add invoiceId to times
			for (Time time : getTimes()) {
				time.setInvoiceId(invoiceId);
				dbContext.update(time, "invoiceId");
			}
			project.setOpen(end.getTime());
			dbContext.update(project, "open");
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			LOG.error("Could not close the project", e);
			Notification.show("Projekt konnte nicht abgeschlossen werden", Type.ERROR_MESSAGE);
			return;
		}

		Notification.show("Projekt wurde erfolgreich abgeschlossen", Type.HUMANIZED_MESSAGE);
		closingView.finshClosing();
	}

	
	private void buildMainLayout() {
		setWidth("-1px");
		setHeight("-1px");
		setMargin(true);
		setSpacing(true);
		
		// invoiceNumberTextField
		invoiceNumberTextField = new TextField();
		invoiceNumberTextField.setLabel("Rechnungsnummer");
		invoiceNumberTextField.setWidth("-1px");
		invoiceNumberTextField.setHeight("-1px");
		add(invoiceNumberTextField);
		
		// nextStart
		nextStart = new DatePicker();

		DateHelper.init(nextStart);
		nextStart.setLabel("Neuer Zeitraum ab");
		nextStart.setWidth("-1px");
		nextStart.setHeight("-1px");
		add(nextStart);
		
		// horizontalLayout_1
		horizontalLayout_1 = buildHorizontalLayout_1();
		add(horizontalLayout_1);
	}

	
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		horizontalLayout_1 = new HorizontalLayout();
		horizontalLayout_1.setWidth("-1px");
		horizontalLayout_1.setHeight("-1px");
		horizontalLayout_1.setMargin(false);
		horizontalLayout_1.setSpacing(true);
		
		// closeProjectButton
		closeProjectButton = new Button();
		closeProjectButton.setText("Abschlieﬂen");
		closeProjectButton.setWidth("-1px");
		closeProjectButton.setHeight("-1px");
		horizontalLayout_1.add(closeProjectButton);
		
		// downloadInvoiceButton
		downloadInvoiceAnchor = new Anchor();
		downloadInvoiceAnchor.setText("Rechnung");
		downloadInvoiceAnchor.setWidth("-1px");
		downloadInvoiceAnchor.setHeight("-1px");
		horizontalLayout_1.add(downloadInvoiceAnchor);
		
		// downloadTimeSheetButton
		downloadTimeSheetAnchor = new Anchor();
		downloadTimeSheetAnchor.setText("Leistungsnachweis");
		downloadTimeSheetAnchor.setWidth("-1px");
		downloadTimeSheetAnchor.setHeight("-1px");
		horizontalLayout_1.add(downloadTimeSheetAnchor);
		
		return horizontalLayout_1;
	}

}
