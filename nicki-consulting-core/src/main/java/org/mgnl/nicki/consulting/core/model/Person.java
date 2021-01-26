package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.core.context.AppContext;
import org.mgnl.nicki.core.context.NickiContext;
import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.SubTable;
import org.mgnl.nicki.db.annotation.Table;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import lombok.Getter;
import lombok.Setter;

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

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		return "Person id=" + id;
	}
	
}
