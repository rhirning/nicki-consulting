package org.mgnl.nicki.consulting.core.helper;

import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.core.context.AppContext;
import org.mgnl.nicki.core.objects.DynamicObjectException;

public class PasswordHelper {
	
	public static enum METHOD {
		SHA("SHA-1"),
		MD5("MD5");
		private String method;
		METHOD(String method) {
			this.method = method;
		}
		public String getMethod() {
			return method;
		}
	}
	public static boolean verifyPassword(LdapPerson person, String password) {
		try {
			return null != AppContext.getSystemContext(person.getContext().getTarget(), person.getPath(), password);
		} catch (InvalidPrincipalException e) {
			return false;
		}
	}
	
	public static void setPassword(LdapPerson person, String password) throws DynamicObjectException {
		person.setUserPassword(password);
		person.update("userPassword");
	}

}
