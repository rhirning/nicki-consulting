package org.mgnl.nicki.consulting.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.PreparedStatementSelectHandler;
import org.mgnl.nicki.db.helper.Type;
import org.mgnl.nicki.db.helper.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSelectHandler extends NonLoggingSelectHandler implements PreparedStatementSelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSelectHandler.class);

	private enum TYPE {PERSON};
	
	private TYPE type ;
	private Person person;
	private Period period;
	private Customer customer;
	private Project project;
	
	private String timeTableName;
	private String memberTableName;
	private String projectTableName;
	
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
			projectTableName = dbContext.getQualifiedTableName(Project.class);
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override
	public String getSearchStatement() {
		if (type == TYPE.PERSON) {
			try {
				return getSearchStatement(person);
			} catch (TimeSelectException e) {
				LOG.error("Error creating select statement", e);
			}
		}
		return null;
	}
	
	public 
	PreparedStatement getPreparedStatement(Connection connection) throws SQLException {
		if (type != TYPE.PERSON) {
			return null;
		}
		List<TypedValue> typedValues = new ArrayList<TypedValue>();
		int count = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(timeTableName).append(" WHERE MEMBER_ID IN ( ");
		if (this.project != null) {
			sb.append("SELECT ID FROM ").append(memberTableName);
			sb.append(" WHERE ");
			if (person.getId() > 0) {
				sb.append("PERSON_ID = ? AND ");
				typedValues.add(new TypedValue(Type.LONG, ++count, person.getId()));
			}
			sb.append("PROJECT_ID = ?");
			typedValues.add(new TypedValue(Type.LONG, ++count, project.getId()));
		} else if (this.customer != null) {
			sb.append("SELECT ID FROM ").append(memberTableName);
			sb.append(" WHERE ");
			if (person.getId() > 0) {
				sb.append("PERSON_ID = ? AND ");
				typedValues.add(new TypedValue(Type.LONG, ++count, person.getId()));
			}
			sb.append("PROJECT_ID IN (");
			sb.append("SELECT ID FROM ").append(projectTableName);
			sb.append(" WHERE CUSTOMER_ID = ?");
			typedValues.add(new TypedValue(Type.LONG, ++count, customer.getId()));
			sb.append(" )");
			
		} else {
			sb.append("SELECT ID FROM ").append(memberTableName);
			if (person.getId() > 0) {
				sb.append(" WHERE PERSON_ID = ?");
				typedValues.add(new TypedValue(Type.LONG, ++count, person.getId()));
			}
		}
		sb.append(" )");
		if (this.period != null) {
			sb.append(" AND START_TIME >= ?");
			typedValues.add(new TypedValue(Type.TIMESTAMP, ++count, period.getStart().getTime()));
			
			sb.append(" AND END_TIME < ?");
			typedValues.add(new TypedValue(Type.TIMESTAMP, ++count, period.getEnd().getTime()));
		}
		sb.append(" ORDER BY START_TIME ASC");
		LOG.debug(sb.toString());
		
		PreparedStatement pstmt = connection.prepareStatement(sb.toString());
		for (TypedValue typedValue : typedValues) {
			typedValue.fillPreparedStatement(pstmt);
		}
		return pstmt;
	}

	public String getSearchStatement(Person person) throws TimeSelectException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(timeTableName).append(" WHERE MEMBER_ID IN ( ");
		if (this.project != null) {
			sb.append("SELECT ID FROM ").append(memberTableName);
			sb.append(" WHERE ");
			if (person.getId() > 0) {
				sb.append("PERSON_ID = ").append(person.getId()).append(" AND ");
			}
			sb.append("PROJECT_ID = ").append(this.project.getId());
		} else if (this.customer != null) {
			sb.append("SELECT ID FROM ").append(memberTableName);
			sb.append(" WHERE ");
			if (person.getId() > 0) {
				sb.append("PERSON_ID = ").append(person.getId()).append(" AND ");
			}
			sb.append("PROJECT_ID IN (");
			sb.append("SELECT ID FROM ").append(projectTableName);
			sb.append(" WHERE CUSTOMER_ID = ").append(customer.getId());
			sb.append(" )");
			
		} else {
			sb.append("SELECT ID FROM ").append(memberTableName);
			if (person.getId() > 0) {
				sb.append(" WHERE PERSON_ID = ").append(person.getId());
			}
		}
		sb.append(" )");
		if (this.period != null) {
			try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
				sb.append(" AND START_TIME >= ").append(dbContext.getDateAsDbString(period.getStart().getTime()));
				sb.append(" AND END_TIME < ").append(dbContext.getDateAsDbString(period.getEnd().getTime()));
			} catch (SQLException e) {
				throw new TimeSelectException(e);
			}
		}
		sb.append(" ORDER BY START_TIME ASC");
		LOG.debug(sb.toString());
		return sb.toString();
	}

	public void handle(ResultSet rs) throws SQLException {
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			while (rs.next()) {
				try {
					list.add(dbContext.get(Time.class, rs));
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.error("Error handling result", e);
				}
			}
		}
	}

	public List<Time> getList() {
		return list;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
