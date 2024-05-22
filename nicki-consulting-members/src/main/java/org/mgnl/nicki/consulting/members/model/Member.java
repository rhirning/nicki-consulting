package org.mgnl.nicki.consulting.members.model;

import java.io.Serializable;
import java.util.Date;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

import lombok.Data;

@Data
@Table(name = "MEMBERS")
public class Member implements Serializable {
	private static final long serialVersionUID = 2587714583373894281L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;

	@Attribute(name = "PERSONAL_TITLE")
	private String personalTitle;

	@Attribute(name = "GIVENNAME")
	private String givenname;

	@Attribute(name = "SURNAME")
	private String surname;

	@Attribute(name = "POST_CODE")
	private String postCode;

	@Attribute(name = "CITY")
	private String city;

	@Attribute(name = "STREET")
	private String street;

	@Attribute(name = "ENTRY_DATE", type = DataType.DATE)
	private Date entryDate;

	@Attribute(name = "MAIL")
	private String mail;

	@Attribute(name = "PHONE")
	private String phone;

	@Attribute(name = "MOBILE")
	private String mobile;

	@Attribute(name = "IBAN")
	private String iban;
	
	public String getDisplayName() {
		StringBuilder sb = new StringBuilder();
		sb.append(surname).append(", ").append(givenname);
		return sb.toString();
	}
}
