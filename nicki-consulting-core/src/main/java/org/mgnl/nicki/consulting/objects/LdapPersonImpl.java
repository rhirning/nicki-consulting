package org.mgnl.nicki.consulting.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.core.annotation.AdditionalObjectClass;
import org.mgnl.nicki.core.annotation.DynamicAttribute;
import org.mgnl.nicki.core.annotation.DynamicObject;
import org.mgnl.nicki.core.annotation.RemoveAdditionalObjectClass;
import org.mgnl.nicki.core.annotation.RemoveDynamicAttribute;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.dynamic.objects.objects.Group;
import org.mgnl.nicki.dynamic.objects.objects.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static final String ATTRIBUTE_TELEPHONE_NUMBER = "telephoneNumber";
	public static final String ATTRIBUTE_SURNAME = "surname";
	public static final String ATTRIBUTE_UNICODE_PWD = "unicodePwd";
	public static final String ATTRIBUTE_USER_PASSWORD = "userPassword";
	public static final String ATTRIBUTE_CARLICENSE = "carLicense";
	public static final String ATTRIBUTE_OU = "ou";

	private Collection<Group> groups;
	
	@DynamicAttribute(externalName = "ou")
	public String getOu() {
		return getAttribute(ATTRIBUTE_OU);
	}

	public void setOu(String value) {
		put(ATTRIBUTE_OU, value);
	}
	
	@DynamicAttribute(externalName = "carLicense")
	public String getCarLicense() {
		return getAttribute(ATTRIBUTE_CARLICENSE);
	}

	public void setCarLicense(String value) {
		put(ATTRIBUTE_CARLICENSE, value);
	}
	
	@DynamicAttribute(externalName = "telephoneNumber")
	public String getTelephoneNumber() {
		return getAttribute(ATTRIBUTE_TELEPHONE_NUMBER);
	}

	public void setTelephoneNumber(String value) {
		put(ATTRIBUTE_TELEPHONE_NUMBER, value);
	}
	
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
	
	public static enum METHOD {SHA,	MD5}
	
	@DynamicAttribute(externalName = "userPassword")
	public String getUserPassword() {
		return getAttribute(ATTRIBUTE_USER_PASSWORD);
	}
	
	@Override
	public void setUserPassword(String userpassword) {
		String encryptedPassword = getEncryptedString(METHOD.MD5, userpassword);
		put(ATTRIBUTE_USER_PASSWORD, encryptedPassword);
	}

	private static String getEncryptedString(METHOD method, String obj) {
		byte[] md5byte = DigestUtils.md5(obj);
		byte[] md5hex = Base64.encodeBase64(md5byte);
		String md5String = new String(md5hex);
		String retVal = "{" + method.toString() + "}" + md5String;
		return retVal;
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


	@Override
	public Collection<Group> getGroups() {
		if (groups == null) {
			try {
				this.groups = getContext().loadObjects(Group.class, Config.getString("nicki.groups.basedn"), "(member=" + getPath() + ")");
			} catch (Exception e) {
				LOG.error("Error", e);
				groups = new ArrayList<Group>();
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

}
