package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Invoice;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceSelectHandler.class);

	private Period period;
	private Customer customer;
	private Project project;
	
	private String invoiceTableName;
	private String projectTableName;
	
	private List<Invoice> list = new ArrayList<>();
	private String dbContextName;
	
	public InvoiceSelectHandler(String dbContextName) throws TimeSelectException {
		super();
		this.dbContextName = dbContextName;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			invoiceTableName = dbContext.getQualifiedTableName(Invoice.class);
			projectTableName = dbContext.getQualifiedTableName(Project.class);
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
		sb.append("SELECT * FROM ").append(invoiceTableName).append(" WHERE");

		if (this.period != null) {
			try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
				sb.append(" INVOICE_DATE >= ").append(dbContext.getDateAsDbString(period.getStart().getTime()));
				sb.append(" AND INVOICE_DATE < ").append(dbContext.getDateAsDbString(period.getEnd().getTime()));
			} catch (SQLException e) {
				throw new TimeSelectException(e);
			}
		}
		if (this.project != null) {
			sb.append(" AND PROJECT_ID = ").append(this.project.getId());
		} else if (this.customer != null) {
			sb.append(" AND PROJECT_ID IN (");
			sb.append("SELECT ID FROM ").append(projectTableName);
			sb.append(" WHERE CUSTOMER_ID = ").append(customer.getId()).append(")");			
		}
		
		sb.append(" ORDER BY INVOICE_DATE ASC");
		LOG.debug(sb.toString());
		return sb.toString();
	}

	public void handle(ResultSet rs) throws SQLException {
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			while (rs.next()) {
				try {
					list.add(dbContext.get(Invoice.class, rs));
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.error("Error handling result", e);
				}
			}
		}
	}

	public List<Invoice> getList() {
		return list;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
