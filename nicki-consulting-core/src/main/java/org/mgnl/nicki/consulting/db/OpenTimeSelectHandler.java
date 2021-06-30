package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenTimeSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(OpenTimeSelectHandler.class);

	private List<OpenProject> projects = new ArrayList<>();
	private String timeTableName;
	private String memberTableName;
	private Project project;
	private Date after;
	private Date before;
	String beforeClause;
	String afterClause;
	private List<Time> list = new ArrayList<>();
	private String dbContextName;
	
	public OpenTimeSelectHandler(Project project, Date afterDate, Date before, String dbContextName) throws TimeSelectException {
		super();
		this.project = project;
		try {
			this.after = afterDate!= null ? afterDate : DataHelper.dateFromDisplayDay("01.01.2000");
		} catch (ParseException e1) {
			// Kann nicht passieren
		}
		this.before = before;
		this.dbContextName =dbContextName;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			timeTableName = dbContext.getQualifiedTableName(Time.class);
			memberTableName = dbContext.getQualifiedTableName(Member.class);
			if (before != null) {
				beforeClause = "DATE(START_TIME) < " + dbContext.toDate(before);

			}
			if (after != null) {
				afterClause = "DATE(START_TIME) >= " + dbContext.toDate(after);
			}
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override
	public String getSearchStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(timeTableName).append(" T");
		sb.append(" WHERE MEMBER_ID IN ");
		sb.append("( SELECT ID FROM ").append(memberTableName).append(" M WHERE PROJECT_ID = ").append(this.project.getId()).append(" )");
		sb.append(" AND INVOICE_ID IS NULL ");
		if (after != null) {
			sb.append(" AND ").append(afterClause);
		}
		if (before != null) {
			sb.append(" AND ").append(beforeClause);
		}
		sb.append(" ORDER BY START_TIME ASC");
		LOG.debug(sb.toString());
		return sb.toString();
	}

	@Override
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

	public List<OpenProject> getProjects() {
		return projects;
	}

	public List<Time> getList() {
		return list;
	}
}
