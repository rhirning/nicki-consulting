package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenProjectsSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(OpenProjectsSelectHandler.class);

	private List<OpenProject> projects = new ArrayList<>();
	private String timeTableName;
	private String memberTableName;
	private String projectTableName;
	private Date before;
	String beforeClause;
	
	public OpenProjectsSelectHandler(Date before, String dbContextName) throws TimeSelectException {
		super();
		this.before = before;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			timeTableName = dbContext.getQualifiedTableName(Time.class);
			memberTableName = dbContext.getQualifiedTableName(Member.class);
			projectTableName = dbContext.getQualifiedTableName(Project.class);
			if (before != null) {
				beforeClause = "DATE(T.START_TIME) < " + dbContext.toDate(before);

			}
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override
	public String getSearchStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT M.PROJECT_ID, SUM(T.HOURS) FROM ");
		sb.append(timeTableName).append(" T , " ).append(memberTableName).append(" M, ").append(projectTableName).append(" P ");
		sb.append(" WHERE T.MEMBER_ID = M.ID AND M.PROJECT_ID = P.ID");
		sb.append(" AND ( P.OPEN_DATE IS NULL OR DATE(T.START_TIME) >= P.OPEN_DATE)");
		sb.append(" AND T.INVOICE_ID IS NULL ");
		if (before != null) {
			sb.append(" AND ").append(beforeClause).append(" ");
		}
		sb.append("GROUP BY PROJECT_ID");
		LOG.debug(sb.toString());
		return sb.toString();
	}

	@Override
	public void handle(ResultSet rs) throws SQLException {
		while (rs.next()) {
			OpenProject openProject = new OpenProject();
			openProject.setProjectId(rs.getLong(1));
			openProject.setHours(rs.getFloat(2));
			projects.add(openProject);
		}
	}

	public List<OpenProject> getProjects() {
		return projects;
	}
}
