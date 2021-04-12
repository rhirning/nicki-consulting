package org.mgnl.nicki.consulting.core.model;

import java.io.Serializable;
import java.util.Date;

import org.mgnl.nicki.db.annotation.Attribute;
import org.mgnl.nicki.db.annotation.ForeignKey;
import org.mgnl.nicki.db.annotation.Table;
import org.mgnl.nicki.db.data.DataType;

import lombok.Data;

@Data
@Table(name="TIME")
public class Time implements Serializable {

	private static final long serialVersionUID = -3201994233663653615L;

	@Attribute(name = "ID", autogen=true, primaryKey=true)
	private Long id;
	
	@Attribute(name = "MEMBER_ID", mandatory = true)
	@ForeignKey(columnName = "ID", foreignKeyClass=Member.class)
	private Long memberId;
	
	@Attribute(name = "INVOICE_ID")
	@ForeignKey(columnName = "ID", foreignKeyClass=Invoice.class)
	private Long invoiceId;

	@Attribute(name = "TEXT", mandatory = true)
	private String text;

	@Attribute(name = "START_TIME", type=DataType.TIMESTAMP)
	private Date start;

	@Attribute(name = "END_TIME", type=DataType.TIMESTAMP)
	private Date end;

	@Attribute(name = "PAUSE")
	private Integer pause;

	@Attribute(name = "HOURS")
	private Float hours;

	@Attribute(name = "CUSTOMER_REPORT")
	private Boolean customerReport;

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
