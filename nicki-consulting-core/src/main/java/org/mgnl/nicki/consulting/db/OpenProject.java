package org.mgnl.nicki.consulting.db;

import java.util.Date;

import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.core.helper.DataHelper;

public class OpenProject {
	private Project project;
	private Customer customer;
	private Long projectId;
	private Float hours;
	private Date since;
	
	public Customer getCustomer() {
		if (this.customer == null) {
			this.customer = TimeHelper.getCustomer(getProject().getCustomerId());
		}
		return this.customer;
	}
	
	public String getProjectName() {
		if (getProject() != null) {
			return this.project.getName();
		} else {
			return "";
		}
	}
	
	public String getCustomerName() {
		if (getCustomer() != null) {
			return this.customer.getName();
		} else {
			return "";
		}
	}
	public Project getProject() {
		if (this.project == null) {
			this.project = TimeHelper.getProject(projectId);
		}
		return this.project;
	}
	public String getHours() {
		if (hours != null) {
			return String.format("%.2f", hours);
		} else {
			return "";
		}
	}
	public void setHours(Float hours) {
		this.hours = hours;
	}
	public String getDays() {
		if (hours != null) {
			return String.format("%.2f", hours/8.0);
		} else {
			return "";
		}
	}
	public String getSince() {
		if (this.since == null) {
			this.since = getProject().getOpen();
		}
		if (this.since == null) {
			return "";
		}
		return DataHelper.getDisplayDay(since);
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

}
