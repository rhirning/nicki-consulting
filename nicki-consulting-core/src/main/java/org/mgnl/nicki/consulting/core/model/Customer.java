package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.List;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.SubTable;
import org.mgnl.nicki.db.annotation.Table;

import lombok.Data;

@Data
@Table(name = "CUSTOMERS")
public class Customer implements Serializable {

	private static final long serialVersionUID = 2432644242405557820L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;

	@Attribute(name = "NAME", mandatory = true)
	private String name;
	
	@Attribute(name = "PARENT_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=Customer.class, display="name")
	private Long parentId;

	@Attribute(name = "ALIAS")
	private String alias;

	@Attribute(name = "STREET")
	private String street;

	@Attribute(name = "ZIP")
	private String zip;

	@Attribute(name = "CITY")
	private String city;

	@Attribute(name = "INVOICE_TEMPLATE")
	private String invoiceTemplate;

	@Attribute(name = "TIMESHEET_TEMPLATE")
	private String timeSheetTemplate;
	
	@SubTable(foreignKey = "customerId")
	private List<Project> projects;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" (").append(id).append(")");
		return sb.toString();
	}
}
