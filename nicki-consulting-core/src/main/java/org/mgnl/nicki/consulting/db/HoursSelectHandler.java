package org.mgnl.nicki.consulting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.handler.NonLoggingSelectHandler;
import org.mgnl.nicki.db.handler.SelectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HoursSelectHandler extends NonLoggingSelectHandler implements SelectHandler {
	private static final Logger LOG = LoggerFactory.getLogger(HoursSelectHandler.class);

	private String timeTableName;
	private float hours;
	
	public HoursSelectHandler(String dbContextName) throws TimeSelectException {
		super();
		try (DBContext dbContext = DBContextManager.getContext(dbContextName)) {
			timeTableName = dbContext.getQualifiedTableName(Time.class);
		} catch (SQLException | NotSupportedException e) {
			throw new TimeSelectException(e);
		}
	}

	@Override
	public String getSearchStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT SUM(HOURS) AS HOURS FROM ").append(timeTableName).append(" WHERE MEMBER_ID IN (");
		sb.append(getMemberIds());
		sb.append(")");
		LOG.debug(sb.toString());
		return sb.toString();
	}
	
	protected abstract String getMemberIds();

	@Override
	public void handle(ResultSet rs) throws SQLException {
		while (rs.next()) {
			hours += rs.getFloat(1);
		}
	}

	public float getHours() {
		return hours;
	}
}
