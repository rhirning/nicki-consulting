package org.mgnl.nicki.consulting.core.model;

import java.util.Date;
import java.util.List;

import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.SubTable;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

@Table(name = "MEMBERS")
public class Member {
	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "PROJECT_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=Project.class, display="name")
	private Long projectId;
	
	@Attribute(name = "PERSON_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=Person.class, display="name")
	private Long personId;

	@Attribute(name = "ROLE")
	private String role;

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
	
	@SubTable(foreignKey = "memberId")
	private List<Time> time;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

	public List<Time> getTime() {
		return time;
	}

	public void setTime(List<Time> time) {
		this.time = time;
	}

	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		sb.append(TimeHelper.getProjectDisplayName(projectId));
		sb.append(" - ").append(role);
		return sb.toString();
	}

}
