package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.Date;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

@Table(name = "INVOICES")
public class Invoice implements Serializable {
	private static final long serialVersionUID = -2589276383513009338L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "PROJECT_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=Project.class, display="name")
	private Long projectId;

	@Attribute(name = "INVOICE_NUMBER", mandatory=true)
	private String invoiceNumber;

	@Attribute(name = "START_DATE", type=DataType.TIMESTAMP)
	private Date start;

	@Attribute(name = "END_DATE", type=DataType.TIMESTAMP)
	private Date end;

	@Attribute(name = "INVOICE_DATE", type=DataType.TIMESTAMP)
	private Date invoiceDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		sb.append("Rechnung Nr. ").append(invoiceNumber);
		sb.append("(").append(DataHelper.getDisplayDay(start)).append(" - ").append(DataHelper.getDisplayDay(end));
		return sb.toString();
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

}
