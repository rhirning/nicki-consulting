package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.consulting.db.TimeSelectHandler;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;

public abstract class BaseView extends CustomComponent implements View {
	private static final long serialVersionUID = 4599832847213624084L;
	private static final Logger LOG = LoggerFactory.getLogger(BaseView.class);
	private NickiApplication application;
	private Person person;
	private PERIOD timeComboBoxValue;
	
	public NickiApplication getApplication() {
		return application;
	}
	
	public void setApplication(NickiApplication application) {
		this.application = application;
	}

	@Override
	public boolean isModified() {
		return false;
	}
	
	public Person getPerson() throws NoValidPersonException, NoApplicationContextException {
		if (person == null) {
			loadPerson();
		}
		return person;
		
	}
	
	private void loadPerson() throws NoValidPersonException, NoApplicationContextException {

		if (application == null) {
			throw new NoApplicationContextException();
		}
		LdapPerson ldapPerson = (LdapPerson) application.getContext().getLoginContext().getUser();
		Person person = new Person();
		person.setUserId(ldapPerson.getName());

		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			this.person = dbContext.loadObject(person, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load person", e);
			throw new NoValidPersonException(ldapPerson.getDisplayName());
		}
		
	}

	protected List<Member> getMembers(Project project) {
		Member member= new Member();
		member.setProjectId(project.getId());
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			return dbContext.loadObjects(member, true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}

	protected List<Member> getMembers(Person person) {
		Member member= new Member();
		member.setPersonId(person.getId());
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			return dbContext.loadObjects(member, true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}


	protected Collection<Customer> getCustomers(Collection<Member> members) {
		Map<Long, Customer> customers = new HashMap<>();
		if (members != null) {
			for (Member member : members) {
				Project project = TimeHelper.getProject(member.getProjectId());
				Customer customer = TimeHelper.getCustomer(project.getCustomerId());
				customers.put(customer.getId(), customer);
			}
		}
		return customers.values();
	}


	protected Collection<Project> getProjects(Collection<Member> members) {
		Map<Long, Project> projects = new HashMap<>();
		if (members != null) {
			for (Member member : members) {
				Project project = TimeHelper.getProject(member.getProjectId());
				projects.put(project.getId(), project);
			}
		}
		return projects.values();
	}
	


	protected List<Time> getTimes(Person person, PERIOD period, Customer customer, Project project) throws TimeSelectException {
		TimeSelectHandler selectHandler = new TimeSelectHandler(person, "projects");
		selectHandler.setPeriod(period);
		if (customer != null) {
			selectHandler.setCustomer(customer);
		}
		if (project != null) {
			selectHandler.setProject(project);
		}
		

		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			dbContext.select(selectHandler);
			return selectHandler.getList();
		} catch (SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}
	
	public enum READONLY {TRUE, FALSE};

	protected List<TimeWrapper> getTimeWrappers(Person person, PERIOD period, Customer customer, Project project, READONLY readonly, int emptyCount) throws TimeSelectException {
		List<TimeWrapper> timeWrappers = new ArrayList<>();
		List<Member> members = getMembers(person);
		for (Time time : getTimes(person, period, customer, project)) {
			timeWrappers.add(new TimeWrapper(time, members, readonly));
		}
		for (int i = 0; i < emptyCount; i++) {
			timeWrappers.add(new TimeWrapper(new Time(), members, readonly));
		}
		
		return timeWrappers;
	}


	protected void initTimeComboBox(ComboBox timeComboBox) {
		for(PERIOD period : PERIOD.values()) {
			timeComboBox.addItem(period);
			timeComboBox.setItemCaption(period, period.getName());
		}
		timeComboBox.setValue(PERIOD.THIS_MONTH);
		setTimeComboBoxValue(PERIOD.THIS_MONTH);
		timeComboBox.setNullSelectionAllowed(false);
	}

	public PERIOD getTimeComboBoxValue() {
		return timeComboBoxValue;
	}

	public void setTimeComboBoxValue(PERIOD timeComboBoxValue) {
		this.timeComboBoxValue = timeComboBoxValue;
	}

}