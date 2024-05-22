package org.mgnl.nicki.consulting.members.views;

public class MembersAdminView extends MembersView {
	private static final long serialVersionUID = -8888115934512418108L;
	
	public MembersAdminView() {
		setAdmin(true);
		setImportType(IMPORT.CSV);
	}

}
