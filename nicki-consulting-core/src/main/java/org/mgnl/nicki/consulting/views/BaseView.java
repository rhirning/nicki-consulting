package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.helper.PersonHelper;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Invoice;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.InvoiceWrapper;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.db.InvoiceSelectHandler;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.consulting.db.TimeSelectHandler;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.CustomComponent;

public abstract class BaseView extends CustomComponent implements View {
	private static final long serialVersionUID = 4599832847213624084L;
	private static final Logger LOG = LoggerFactory.getLogger(BaseView.class);
	private NickiApplication application;
	private Person person;
	private PERIOD timeComboBoxValue;
	private Person personComboBoxValue;
	
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

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			this.person = dbContext.loadObject(person, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load person", e);
			throw new NoValidPersonException(ldapPerson.getDisplayName());
		}
		
	}
	
	public boolean isAdmin() {
		LdapPerson ldapPerson = (LdapPerson) application.getContext().getLoginContext().getUser();
		return ldapPerson.isMemberOf(Config.getString("nicki.consulting.group.admin"));
	}

	protected List<Member> getMembers(Project project) {
		Member member= new Member();
		member.setProjectId(project.getId());
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(member, true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}

	protected List<Member> getMembers(Person person, Period period) {
		Member member= new Member();
		if (person.getId() > 0) {
			member.setPersonId(person.getId());
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			String filter = getMemberPeriodFilter(dbContext, period);
			String orderBy = null;
			return dbContext.loadObjects(member, true, filter, orderBy);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException | NotSupportedException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}
	
	public String getMemberPeriodFilter(DBContext dbContext, Period period) throws NotSupportedException {

		String memberTableName = dbContext.getQualifiedTableName(Member.class);
		String projectTableName = dbContext.getQualifiedTableName(Project.class);
		
		StringBuilder sb = new StringBuilder();
		sb.append("ID IN (");
		sb.append("select m.id from ").append(memberTableName).append(" m, ").append(projectTableName).append(" p");
		sb.append(" where m.PROJECT_ID = p.ID");
		sb.append(" AND (m.START_DATE IS NULL OR m.START_DATE < ").append(dbContext.toDate(period.getEnd().getTime())).append(")");
		sb.append(" AND (p.START_DATE IS NULL OR p.START_DATE < ").append(dbContext.toDate(period.getEnd().getTime())).append(")");
		sb.append(" AND (m.END_DATE IS NULL OR m.END_DATE >= ").append(dbContext.toDate(period.getStart().getTime())).append(")");
		sb.append(" AND (p.END_DATE IS NULL OR p.END_DATE >= ").append(dbContext.toDate(period.getStart().getTime())).append(")");
		sb.append(")");
		return sb.toString();
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


	protected Collection<Project> getProjects(Customer customer, Collection<Member> members) {
		Map<Long, Project> projects = new HashMap<>();
		if (members != null) {
			for (Member member : members) {
				Project project = TimeHelper.getProject(member.getProjectId());
				if (customer == null || customer.getId() == project.getCustomerId()) {
					projects.put(project.getId(), project);
				}
			}
		}
		return projects.values();
	}
	


	protected List<Time> getTimes(Person person, Period period, Customer customer, Project project) throws TimeSelectException {
		TimeSelectHandler selectHandler = new TimeSelectHandler(person, Constants.DB_CONTEXT_NAME);
		selectHandler.setPeriod(period);
		if (customer != null) {
			selectHandler.setCustomer(customer);
		}
		if (project != null) {
			selectHandler.setProject(project);
		}
		

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			dbContext.select(selectHandler);
			return selectHandler.getList();
		} catch (SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}

	protected List<Invoice> getInvoices(Period period, Customer customer, Project project) throws TimeSelectException {
		InvoiceSelectHandler selectHandler = new InvoiceSelectHandler(Constants.DB_CONTEXT_NAME);
		if (period != null) {
			selectHandler.setPeriod(period);
		}
		if (customer != null) {
			selectHandler.setCustomer(customer);
		}
		if (project != null) {
			selectHandler.setProject(project);
		}
		

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			dbContext.select(selectHandler);
			return selectHandler.getList();
		} catch (SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}
	
	public enum READONLY {TRUE, FALSE};

	protected List<TimeWrapper> getTimeWrappers(Person person, Period period, Customer customer, Project project, READONLY readonly, int emptyCount) throws TimeSelectException {
		List<TimeWrapper> timeWrappers = new ArrayList<>();
		List<Member> members = getMembers(person, period);
		for (Time time : getTimes(person, period, customer, project)) {
			Person timePerson = TimeHelper.getPersonFromMemberId(time.getMemberId());
			timeWrappers.add(new TimeWrapper(timePerson, time, members, readonly));
		}
		for (int i = 0; i < emptyCount; i++) {
			timeWrappers.add(new TimeWrapper(person, new Time(), members, readonly));
		}
		
		return timeWrappers;
	}

	protected List<InvoiceWrapper> getInvoiceWrappers(Period period, Customer customer, Project project) throws TimeSelectException {
		List<InvoiceWrapper> invoiceWrappers = new ArrayList<>();
		for (Invoice invoice: getInvoices(period, customer, project)) {
			invoiceWrappers.add(new InvoiceWrapper(invoice, () -> reload()));
		}
		
		return invoiceWrappers;
	}

	protected void reload() {
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

	protected enum ALL {TRUE, FALSE}

	protected void initPersonComboBox(ComboBox personComboBox, ALL withAllEntry) throws NoValidPersonException, NoApplicationContextException {
		Person self = getPerson();
		if (isAdmin()) {
			for(Person person : PersonHelper.getPersons()) {
				personComboBox.addItem(person);
				personComboBox.setItemCaption(person, person.getName());
				if (self.getId() == person.getId()) {
					personComboBox.setValue(person);
					setPersonComboBoxValue(person);
				}
			}
			if (withAllEntry == ALL.TRUE) {
				// ALL
				Person all = new Person();
				all.setId(-1L);
				personComboBox.addItem(all);
				personComboBox.setItemCaption(all, "Alle");
			}
		} else {
			personComboBox.addItem(self);
			personComboBox.setItemCaption(self, self.getName());
			personComboBox.setValue(self);
			setPersonComboBoxValue(self);
			personComboBox.setEnabled(false);
		}
		personComboBox.setNullSelectionAllowed(false);
	}

	public PERIOD getTimeComboBoxValue() {
		return timeComboBoxValue;
	}

	public void setTimeComboBoxValue(PERIOD timeComboBoxValue) {
		this.timeComboBoxValue = timeComboBoxValue;
	}

	public Person getPersonComboBoxValue() {
		return personComboBoxValue;
	}

	public void setPersonComboBoxValue(Person personComboBoxValue) {
		this.personComboBoxValue = personComboBoxValue;
	}

}
