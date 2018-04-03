package org.mgnl.nicki.consulting.core.model;

import java.util.Date;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

@Table(name="TIME")
public class Time {
	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "MEMBER_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=Member.class)
	private Long memberId;

	@Attribute(name = "TEXT")
	private String text;

	@Attribute(name = "START_TIME", type=DataType.TIMESTAMP)
	private Date start;

	@Attribute(name = "END_TIME", type=DataType.TIMESTAMP)
	private Date end;

	@Attribute(name = "PAUSE")
	private Integer pause;

	@Attribute(name = "HOURS")
	private Float hours;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Float getHours() {
		return hours;
	}

	public void setHours(Float hours) {
		this.hours = hours;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Integer getPause() {
		return pause;
	}

	public void setPause(Integer pause) {
		this.pause = pause;
	}

	public int getUniqueHash() {
		StringBuilder sb = new StringBuilder();
		sb.append(memberId == null ? "null" : memberId);
		sb.append(start == null ? "null" : start.getTime());
		sb.append(end == null ? "null" : end.getTime());
		sb.append(pause == null ? "null" : pause);
		sb.append(hours == null ? "null" : hours);
		sb.append(text == null ? "null" : text);
		return sb.toString().hashCode();
	}

}
