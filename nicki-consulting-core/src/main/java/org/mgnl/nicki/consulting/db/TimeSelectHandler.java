package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.views.PERIOD;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSelectHandler.class);

	private enum TYPE {PERSON};
	
	private TYPE type ;
	private Person person;
	private PERIOD period;
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

	public String getSearchStatement(Person person) throws TimeSelectException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(timeTableName).append(" WHERE MEMBER_ID IN ( ");
		if (this.project != null) {
			sb.append("SELECT ID FROM ").append(memberTableName);
			sb.append(" WHERE PERSON_ID = ").append(person.getId());
			sb.append(" AND PROJECT_ID = ").append(this.project.getId());
		} else if (this.customer != null) {
			sb.append("SELECT ID FROM ").append(memberTableName);
			sb.append(" WHERE PERSON_ID = ").append(person.getId());
			sb.append(" AND PROJECT_ID IN (");
			sb.append("SELECT ID FROM ").append(projectTableName);
			sb.append(" WHERE CUSTOMER_ID = ").append(customer.getId());
			sb.append(" )");
			
		} else {
			sb.append("SELECT ID FROM ").append(memberTableName).append(" WHERE PERSON_ID = ").append(person.getId()).append(" )");
		}
		sb.append(" )");
		if (this.period != null) {
			try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
				sb.append(" AND START_TIME >= ").append(dbContext.getDateAsDbString(period.getStart()));
				sb.append(" AND END_TIME < ").append(dbContext.getDateAsDbString(period.getEnd()));
			} catch (SQLException e) {
				throw new TimeSelectException(e);
			}
		}
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

	public void setPeriod(PERIOD period) {
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
