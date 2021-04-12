package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.Date;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

import lombok.Data;

@Data
@Table(name = "PROJECTS")
public class Project implements Serializable {

	private static final long serialVersionUID = -8633682220401303041L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "CUSTOMER_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=Customer.class, display="name")
	private Long customerId;

	@Attribute(name = "NAME", mandatory = true)
	private String name;

	@Attribute(name = "CUSTOMER_REFERENCE", mandatory = true)
	private String reference;

	@Attribute(name = "CONTACT", mandatory = true)
	private String contact;

	@Attribute(name = "PHONE")
	private String phone;

	@Attribute(name = "EMAIL")
	private String email;

	@Attribute(name = "DAYS")
	private Integer days;

	@Attribute(name = "RATE")
	private Float rate;

	@Attribute(name = "ACTIVE")
	private Boolean active;

	@Attribute(name = "VACATION")
	private Boolean vacation;

	@Attribute(name = "CUSTOMER_REPORT")
	private Boolean customerReport;

	@Attribute(name = "START_DATE", type=DataType.DATE)
	private Date start;

	@Attribute(name = "END_DATE", type=DataType.DATE)
	private Date end;

	@Attribute(name = "OPEN_DATE", type=DataType.DATE)
	private Date open;

	@Attribute(name = "INVOICE_TEMPLATE")
	private String invoiceTemplate;

	@Attribute(name = "TIMESHEET_TEMPLATE")
	private String timeSheetTemplate;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" (").append(id).append(")");
		return sb.toString();
	}
}
