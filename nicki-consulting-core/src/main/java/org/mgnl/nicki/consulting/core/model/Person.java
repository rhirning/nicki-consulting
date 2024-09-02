package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.core.context.AppContext;
import org.mgnl.nicki.core.context.NickiContext;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.SubTable;
import org.mgnl.nicki.db.annotation.Table;

import lombok.Data;
import lombok.Setter;

@Data
@Table(name = "PERSONS")
public class Person implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 640700965688393092L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "USER_ID", mandatory = true)
	private String userId;
	
	@SubTable(foreignKey = "personId")
	private List<Member> members;
	private LdapPerson ldapPerson;
	
	private @Setter String displayName;

	public Person() {
	}
	
	public String getDisplayName() {
		if (StringUtils.isNotBlank(displayName)) {
			return displayName;
		} else {
			return getName();
		}
	}
	
	public String getName() {
		load();
		if (ldapPerson != null) {
			return ldapPerson.getDisplayName();
		}
		else {
			return null;
		}
	}

	private void load() {
		if (ldapPerson == null) {
			try {
				NickiContext context = AppContext.getSystemContext();
				List<? extends LdapPerson> objects = context.loadObjects(LdapPerson.class, context.getTarget().getBaseDn(), "cn=" + userId);
				if (objects != null && objects.size() == 1) {
					ldapPerson = objects.get(0);
				}
			} catch (InvalidPrincipalException e) {
				e.printStackTrace();
			}
		}
	}

	public String getEmail() {
		load();
		if (ldapPerson != null) {
			return ldapPerson.getMail();
		}
		else {
			return null;
		}
	}

	@Override
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		return "Person id=" + id;
	}
	
}
