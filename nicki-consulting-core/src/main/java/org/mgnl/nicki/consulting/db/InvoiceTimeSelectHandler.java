package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceTimeSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceTimeSelectHandler.class);

	
	private Long invoiceId;
	
	private String timeTableName;
	
	private List<Time> list = new ArrayList<>();
	private String dbContextName;
	
	public InvoiceTimeSelectHandler(Long invoiceId, String dbContextName) throws TimeSelectException {
		super();
		this.invoiceId = invoiceId;
		this.dbContextName = dbContextName;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			timeTableName = dbContext.getQualifiedTableName(Time.class);
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override

	public String getSearchStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(timeTableName).append(" WHERE INVOICE_ID = ").append(invoiceId);
		LOG.debug(sb.toString());
		return sb.toString();
	}

	public void handle(ResultSet rs) throws SQLException {
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			while (rs.next()) {
				try {
					list.add(dbContext.get(Time.class, rs));
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.error("Error handling result", e);
				}
			}
		}
	}

	public List<Time> getList() {
		return list;
	}
}
