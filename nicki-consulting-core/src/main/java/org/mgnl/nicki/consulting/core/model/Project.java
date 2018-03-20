package org.mgnl.nicki.consulting.core.model;

import java.util.Date;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

@Table(name = "PROJECTS")
public class Project {
	
	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "CUSTOMER_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=Customer.class, display="name")
	private Long customerId;

	@Attribute(name = "NAME")
	private String name;

	@Attribute(name = "CUSTOMER_REFERENCE")
	private String reference;

	@Attribute(name = "CONTACT")
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

}
