package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.core.data.TreeObject;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.SubTable;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

import lombok.Data;

@Data
@Table(name = "MEMBERS")
public class Member implements Serializable, TreeObject {
	private static final long serialVersionUID = -2589276383513009338L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "PROJECT_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=Project.class, display="name")
	private Long projectId;
	
	@Attribute(name = "PERSON_ID", mandatory = true)
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

	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append(TimeHelper.getProjectDisplayName(projectId));
		if (StringUtils.isNotBlank(role)) {
			sb.append(" - ").append(role);
		}
		return sb.toString();
	}

	@Override
	public String getDisplayName() {
		return toString();
	}

}
