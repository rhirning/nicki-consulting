package org.mgnl.nicki.consulting.objects;

import org.mgnl.nicki.core.objects.DynamicObject;

public interface LdapPerson extends DynamicObject {
	
	String getFullName();
	String getSurName();
	String getGivenName();
	String getMail();
	String getMobile();
	String getTelephoneNumber();
	String getCarLicense();
	String getOu();
	
	void setSurName(String surName);
	void setGivenName(String grivenName);
	void setMail(String mail);
	void setMobile(String mobile);
	void setUserPassword(String userpassword);
	void setTelephoneNumber(String telephoneNumber);
	void setOu(String ou);

	boolean isMemberOf(String groupName);
}
