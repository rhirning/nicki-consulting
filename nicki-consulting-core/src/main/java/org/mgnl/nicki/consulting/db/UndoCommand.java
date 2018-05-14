package org.mgnl.nicki.consulting.db;

import java.sql.SQLException;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.model.Invoice;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.Reloader;
import org.mgnl.nicki.core.i18n.I18n;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.command.Command;
import org.mgnl.nicki.vaadin.base.command.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndoCommand implements Command {

	private static final Logger LOG = LoggerFactory.getLogger(UndoCommand.class);
	private Invoice invoice;
	private Reloader reloader;

	public UndoCommand(Invoice invoice, Reloader reloader) {
		super();
		this.invoice = invoice;
		this.reloader = reloader;
	}

	@Override
	public void execute() throws CommandException {

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			// remove invoiceIds from Times
			dbContext.executeUpdate(getUpdateTimeCommand(dbContext));
			
			// set project.open to invoice.start
			dbContext.executeUpdate(getUpdateProjectCommand(dbContext));
			
			// delete invoice
			dbContext.delete(invoice);
			
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			LOG.error("Could not undo invoice", e);
			throw new CommandException(e);
		}
		reloader.reload();
	}

	private String getUpdateTimeCommand(DBContext dbContext) throws NotSupportedException {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(dbContext.getQualifiedTableName(Time.class));
		sb.append(" SET INVOICE_ID=NULL WHERE INVOICE_ID=").append(invoice.getId());
		return sb.toString();
	}

	private String getUpdateProjectCommand(DBContext dbContext) throws NotSupportedException {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(dbContext.getQualifiedTableName(Project.class));
		sb.append(" SET OPEN_DATE=").append(dbContext.toDate(invoice.getStart()));
		sb.append(" WHERE ID=").append(invoice.getProjectId());
		return sb.toString();
	}

	public String getTitle() {
		return I18n.getText("org.mgnl.nicki.consulting.invoice.undo.title");
	}

	public String getHeadline() {
		return I18n.getText("org.mgnl.nicki.consulting.invoice.undo.headline");
	}

	public String getCancelCaption() {
		return I18n.getText("org.mgnl.nicki.consulting.invoice.undo.cancel");
	}

	public String getConfirmCaption() {
		return I18n.getText("org.mgnl.nicki.consulting.invoice.undo.confirm");
	}

	public String getErrorText() {
		return I18n.getText("org.mgnl.nicki.consulting.invoice.undo.error");
	}

}
