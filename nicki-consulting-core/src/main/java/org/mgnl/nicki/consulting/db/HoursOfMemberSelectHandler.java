package org.mgnl.nicki.consulting.db;

import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.db.handler.SelectHandler;

public class HoursOfMemberSelectHandler extends HoursSelectHandler implements SelectHandler {

	private Member member;
	
	public HoursOfMemberSelectHandler(Member member, String dbContextName) throws TimeSelectException {
		super(dbContextName);
		this.member = member;
	}

	protected String getMemberIds() {
		return Long.toString(member.getId());
	}
}
