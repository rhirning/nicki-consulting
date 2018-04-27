package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.Date;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public Float getRate() {
		return rate;
	}

	public void setRate(Float rate) {
		this.rate = rate;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" (").append(id).append(")");
		return sb.toString();
	}

	public Date getOpen() {
		return open;
	}

	public void setOpen(Date open) {
		this.open = open;
	}

	public String getInvoiceTemplate() {
		return invoiceTemplate;
	}

	public void setInvoiceTemplate(String invoiceTemplate) {
		this.invoiceTemplate = invoiceTemplate;
	}

	public String getTimeSheetTemplate() {
		return timeSheetTemplate;
	}

	public void setTimeSheetTemplate(String timeSheetTemplate) {
		this.timeSheetTemplate = timeSheetTemplate;
	}

}
