package org.mgnl.nicki.consulting.db;

import java.sql.SQLException;

import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoursOfProjectSelectHandler extends HoursSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(HoursOfProjectSelectHandler.class);

	private Project project;
	private String memberTableName;
	
	public HoursOfProjectSelectHandler(Project project, String dbContextName) throws TimeSelectException {
		super(dbContextName);
		this.project = project;
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			memberTableName = dbContext.getQualifiedTableName(Member.class);
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override
	public String getMemberIds() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ID FROM ").append(memberTableName);
		sb.append(" WHERE PROJECT_ID = ").append(this.project.getId());
		LOG.debug(sb.toString());
		return sb.toString();
	}
}
