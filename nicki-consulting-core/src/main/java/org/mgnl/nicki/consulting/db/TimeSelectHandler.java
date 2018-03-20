package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;

public class TimeSelectHandler extends NonLoggingSelectHandler implements SelectHandler {

	private enum TYPE {PERSON};
	
	private TYPE type ;
	private Person person;
	
	private String timeTableName;
	private String memberTableName;
	
	private List<Time> list = new ArrayList<>();
	private String dbContextName;
	
	public TimeSelectHandler(Person person, String dbContextName) throws TimeSelectException {
		super();
		this.type = TYPE.PERSON;
		this.person = person;
		this.dbContextName = dbContextName;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			timeTableName = dbContext.getQualifiedTableName(Time.class);
			memberTableName = dbContext.getQualifiedTableName(Member.class);
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	public String getSearchStatement() {
		if (type == TYPE.PERSON) {
			return getSearchStatement(person);
		} else {
			return null;
		}
	}

	public String getSearchStatement(Person person) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(timeTableName).append(" WHERE MEMBER_ID IN ( ");
		sb.append("SELECT ID FROM ").append(memberTableName).append(" WHERE PERSON_ID = ").append(person.getId()).append(" )");
		return sb.toString();
	}

	public void handle(ResultSet rs) throws SQLException {
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			while (rs.next()) {
				try {
					list.add(dbContext.get(Time.class, rs));
				} catch (InstantiationException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public List<Time> getList() {
		return list;
	}
}
