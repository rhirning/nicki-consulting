package org.mgnl.nicki.consulting.core.helper;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.views.PersonSelector;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonHelper implements Serializable {

	private static final long serialVersionUID = 5160906181634094846L;
	private static final Logger LOG = LoggerFactory.getLogger(PersonSelector.class);

	public static List<Person> getPersons() {
		Person person = new Person();
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			return dbContext.loadObjects(person, false);

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load persons", e);
		}
		return new ArrayList<Person>();
		
	}

	public static Optional<Person> getPerson(String userId) {
		Person person = new Person();
		person.setUserId(userId);;
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			return Optional.ofNullable(dbContext.loadObject(person, false));

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load persons", e);
		}
		return Optional.empty();
		
	}
}
