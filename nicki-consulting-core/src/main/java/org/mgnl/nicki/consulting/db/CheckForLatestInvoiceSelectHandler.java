package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.mgnl.nicki.consulting.core.model.Invoice;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckForLatestInvoiceSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(CheckForLatestInvoiceSelectHandler.class);

	private Invoice invoice;
	
	private String invoiceTableName;
	
	private Boolean latest;
	
	private String dbContextName;
	
	public CheckForLatestInvoiceSelectHandler(Invoice invoice, String dbContextName) throws TimeSelectException {
		super();
		this.invoice = invoice;
		this.dbContextName = dbContextName;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			invoiceTableName = dbContext.getQualifiedTableName(Invoice.class);
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override
	public String getSearchStatement() {
		try {
			return getInvoiceSearchStatement();
		} catch (TimeSelectException e) {
			LOG.error("Error creating select statement", e);
		}
		return null;
	}

	public String getInvoiceSearchStatement() throws TimeSelectException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) FROM ").append(invoiceTableName).append(" WHERE");
		sb.append(" PROJECT_ID=").append(invoice.getProjectId());
		sb.append(" AND NOT ID=").append(invoice.getId());
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			sb.append(" AND END_DATE > ").append(dbContext.getDateAsDbString(invoice.getEnd()));
		} catch (SQLException e) {
			throw new TimeSelectException(e);
		}
		LOG.debug(sb.toString());
		return sb.toString();
	}

	public void handle(ResultSet rs) throws SQLException {
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			if (rs.next()) {
				latest = rs.getInt(1) == 0;
			}
		}
	}

	public Boolean isLatest() {
		return latest;
	}

}
