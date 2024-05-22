package org.mgnl.nicki.consulting.members.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.members.model.Member;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MembersHelper {

	public static <T> void delete(T bean) throws SQLException, InitProfileException, NotSupportedException {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			dbContext.delete(bean);
		}		
	}

	public static Collection<Member> getAllMembers() {
		Member sales = new Member();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(sales, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
}
