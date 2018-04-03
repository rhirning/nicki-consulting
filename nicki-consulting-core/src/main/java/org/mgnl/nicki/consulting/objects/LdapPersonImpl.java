package org.mgnl.nicki.consulting.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.core.annotation.AdditionalObjectClass;
import org.mgnl.nicki.core.annotation.DynamicAttribute;
import org.mgnl.nicki.core.annotation.DynamicObject;
import org.mgnl.nicki.core.annotation.RemoveAdditionalObjectClass;
import org.mgnl.nicki.core.annotation.RemoveDynamicAttribute;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.dynamic.objects.objects.Group;
import org.mgnl.nicki.dynamic.objects.objects.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@DynamicObject

@RemoveDynamicAttribute({ "isManager", "location", "assignedArticle", "attributeValue", "fullName", "surname", "member",
		"language" })
@RemoveAdditionalObjectClass({ "nickiUserAux" })
@AdditionalObjectClass({ "organizationalPerson", "user"})
public class LdapPersonImpl extends Person implements LdapPerson {
	private static final long serialVersionUID = 4326387747856728648L;
	private static final Logger LOG = LoggerFactory.getLogger(LdapPersonImpl.class);
	public static final String ATTRIBUTE_BIRTHDATE = "birthDate";
	public static final String ATTRIBUTE_LOCATION = "location";
	public static final String ATTRIBUTE_MAIL = "mail";
	public static final String ATTRIBUTE_MOBILE = "mobile";
	public static final String ATTRIBUTE_SURNAME = "surname";
	public static final String ATTRIBUTE_UNICODE_PWD = "unicodePwd";

	private Collection<Group> groups;
	
	@DynamicAttribute(externalName = "mobile")
	public String getMobile() {
		return getAttribute(ATTRIBUTE_MOBILE);
	}

	public void setMobile(String value) {
		put(ATTRIBUTE_MOBILE, value);
	}

	@DynamicAttribute(externalName = "mail")
	public String getMail() {
		return getAttribute(ATTRIBUTE_MAIL);
	}

	public void setMail(String value) {
		put(ATTRIBUTE_MAIL, value);
	}


	@DynamicAttribute(externalName = ATTRIBUTE_UNICODE_PWD, type = byte[].class)
	public byte[] getUnicodePwd() {
		return new byte[] {};
	}

	public void setUnicodePwd(String password) {
		try {
			byte[] newPass = ('"' + password + '"').getBytes("UTF-16LE");

			put(ATTRIBUTE_UNICODE_PWD, newPass);
		} catch (Exception e) {
			LOG.error("error coding password", e);
		}
	}

	@DynamicAttribute(externalName = "sn")
	public String getSurname() {
		return getAttribute(ATTRIBUTE_SURNAME);
	}

	public void setSurname(String value) {
		put(ATTRIBUTE_SURNAME, value);
	}

	@DynamicAttribute(externalName = "l")
	public String getLocation() {
		return getAttribute(ATTRIBUTE_LOCATION);
	}

	public void setLocation(String value) {
		put(ATTRIBUTE_LOCATION, value);
	}

	public Date getBirthDate() {
		try {
			return DataHelper.dateFromDisplayDay((String) get(ATTRIBUTE_BIRTHDATE));
		} catch (Exception ex) {
			return null;
		}
	}

	public void setBirthDate(Date value) {
		if (value != null) {
			put(ATTRIBUTE_BIRTHDATE, DataHelper.getDisplayDay(value));
		} else {
			clear(ATTRIBUTE_BIRTHDATE);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public Collection<Group> getGroups() {
		if (groups == null) {
			TemplateMethodModelEx method = (TemplateMethodModelEx) get("getGroups");
			if (method != null) {
				try {
					groups = (Collection<Group>) method.exec(null);
				} catch (TemplateModelException e) {
					LOG.error("Error", e);
					groups = new ArrayList<Group>();
				}
			}
		}
		return groups;
	}

	@Override
	public boolean isMemberOf(String groupName) {
		for (Group group : getGroups()) {
			if (StringUtils.equalsIgnoreCase(group.getName(), groupName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSurName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSurName(String surName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUserPassword(String userpassword) {
		// TODO Auto-generated method stub
		
	}

}
