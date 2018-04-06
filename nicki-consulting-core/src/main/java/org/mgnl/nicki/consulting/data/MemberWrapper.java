package org.mgnl.nicki.consulting.data;

import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;

public class MemberWrapper {
	private Member member;
	private Person person;
	private Project project;
	

	public MemberWrapper(Member member) {
		this.member = member;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}
	
	protected Person getPerson() {
		if (person == null) {
			person = TimeHelper.getPersonFromMemberId(member.getId());
		}
		return person;
	}
	
	public String getPersonName() {
		return getPerson().getName();
	}
	
	

	protected Project getProject() {
		if (project == null) {
			project = TimeHelper.getProjectFromMemberId(member.getId());
		}
		return project;
	}

}
