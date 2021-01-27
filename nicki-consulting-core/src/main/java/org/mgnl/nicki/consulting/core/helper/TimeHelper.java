package org.mgnl.nicki.consulting.core.helper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.db.HoursOfMemberSelectHandler;
import org.mgnl.nicki.consulting.db.HoursOfProjectSelectHandler;
import org.mgnl.nicki.consulting.db.OpenProject;
import org.mgnl.nicki.consulting.db.OpenProjectsSelectHandler;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeHelper {
	private static final Logger LOG = LoggerFactory.getLogger(TimeHelper.class);
	public final static String FORMAT_TIME = "HH:mm";

	public static Object getProjectDisplayName(Long projectId) {
		StringBuilder sb = new StringBuilder();
		Project project = getProject(projectId);
		if (project != null) {
			Customer customer = getCustomer(project.getCustomerId());
			if (customer != null) {
				sb.append(customer.getName()).append(" - ");
			}
			sb.append(project.getName());
		}
		if (sb.length() == 0) {
			sb.append("invalid project id:").append(projectId);
		}
		return sb.toString();
	}

	public static Project getProject(Long id) {
		Project project = new Project();
		project.setId(id);
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(project, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load project", e);
		}
		return null;
	}
	
	public static float getHoursForMember(Member member) {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			HoursOfMemberSelectHandler handler = new HoursOfMemberSelectHandler(member, Constants.DB_CONTEXT_NAME);
			dbContext.select(handler);
			return handler.getHours();
		} catch (SQLException | InitProfileException | TimeSelectException e) {
			LOG.error("Could not get hours for member", e);
		}
		return -1;
	}
	
	public static float getHoursForProject(Project project) {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			HoursOfProjectSelectHandler handler = new HoursOfProjectSelectHandler(project, Constants.DB_CONTEXT_NAME);
			dbContext.select(handler);
			return handler.getHours();
		} catch (SQLException | InitProfileException | TimeSelectException e) {
			LOG.error("Could not get hours for member", e);
		}
		return -1;
	}
	
	public static List<OpenProject> getOpenProjects(Date before) {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			OpenProjectsSelectHandler handler = new OpenProjectsSelectHandler(before, Constants.DB_CONTEXT_NAME);
			dbContext.select(handler);
			return handler.getProjects();
		} catch (SQLException | InitProfileException | TimeSelectException e) {
			LOG.error("Could not get hours for member", e);
		}
		return new ArrayList<>();
	}
	
	public static float getHoursFromTimeList(List<Time> times) {
		float hours = 0;
		for (Time time : times) {
			hours += time.getHours();
		}
		return hours;
	}
	
	public static float getHoursFromTimeWrapperList(Collection<TimeWrapper> timeWrappers) {
		float hours = 0;
		for (TimeWrapper timeWrapper : timeWrappers) {
			hours += timeWrapper.getTime().getHours();
		}
		return hours;
	}
	
	public static float getDaysFromTimeWrapperList(List<TimeWrapper> timeWrappers) {
		return getHoursFromTimeWrapperList(timeWrappers) / 8.0f;
	}
	
	public static float getDaysFromTimeList(List<Time> times) {
		return getHoursFromTimeList(times) / 8.0f;
	}
	
	public static List<TimeWrapper> filter(List<TimeWrapper> timeWrappers, Member member) {
		List<TimeWrapper> list = new ArrayList<>();
		for (TimeWrapper timeWrapper : timeWrappers) {
			if (timeWrapper.getTime().getMemberId() == member.getId()) {
				list.add(timeWrapper);
			}
		}
		return list;
	}
	
	public static List<TimeWrapper> filterTimeWrappers(List<TimeWrapper> timeWrappers, long memberId) {
		List<TimeWrapper> list = new ArrayList<>();
		for (TimeWrapper timeWrapper : timeWrappers) {
			if (timeWrapper.getTime().getMemberId() == memberId) {
				list.add(timeWrapper);
			}
		}
		return list;
	}
	
	public static List<Time> filterTimes(List<Time> times, long memberId) {
		List<Time> list = new ArrayList<>();
		for (Time time : times) {
			if (time.getMemberId() == memberId) {
				list.add(time);
			}
		}
		return list;
	}
	
	public static List<Long> getMemberIdsFromTimeWrappers(List<TimeWrapper> timeWrappers) {
		List<Long> list = new ArrayList<>();
		for (TimeWrapper timeWrapper : timeWrappers) {
			if (!list.contains(timeWrapper.getTime().getMemberId())) {
				list.add(timeWrapper.getTime().getMemberId());
			}
		}
		return list;
	}
	
	public static List<Long> getMemberIdsFromTimes(List<Time> times) {
		List<Long> list = new ArrayList<>();
		for (Time time : times) {
			if (!list.contains(time.getMemberId())) {
				list.add(time.getMemberId());
			}
		}
		return list;
	}
	
	public static float getVacationHoursFromTimeWrapperList(List<TimeWrapper> timeWrappers) {
		float hours = 0;
		for (TimeWrapper timeWrapper : timeWrappers) {
			if (timeWrapper.isVacation()) {
				hours += timeWrapper.getTime().getHours();
			}
		}
		return hours;
	}
	
	public static String formatHours(float hours) {

        return String.format("%.2f",hours);
	}

	public static Project getProjectFromMemberId(Long memberId) {
		Member member = new Member();
		member.setId(memberId);
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			member = dbContext.loadObject(member, false);
			Project project = new Project();
			project.setId(member.getProjectId());
			return dbContext.loadObject(project, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load project", e);
		}
		return null;
	}

	public static Person getPersonFromMemberId(Long memberId) {
		Member member = new Member();
		member.setId(memberId);
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			member = dbContext.loadObject(member, false);
			Person person = new Person();
			person.setId(member.getPersonId());
			return dbContext.loadObject(person, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load project", e);
		}
		return null;
	}
	
	public static Customer getCustomer(Long id) {
		Customer customer = new Customer();
		if (id != null) {
			customer.setId(id);
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(customer, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load customer", e);
		}
		return null;
	}
	
	public static List<Customer> getAllCustomers() {
		Customer customer = new Customer();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(customer, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
	
	public static List<Person> getAllPersons() {
		Person person = new Person();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(person, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
	
	public static List<Member> getAllMembers() {
		return getMembers(null);
	}
	
	public static List<Project> getAllProjects() {
		return getProjects(null);
	}

	public static List<Member> getMembers(Project project) {
		Member member= new Member();
		if (project != null) {
			member.setProjectId(project.getId());
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(member, true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}
	
	public static List<Project> getProjects(Customer customer) {
		Project project = new Project();
		if (customer != null) {
			project.setCustomerId(customer.getId());
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(project, true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
	}

	public static Map<Long, Member> getAllMembersMap() {
		Map<Long, Member> map = new HashMap<>();
		List<Member> members = TimeHelper.getAllMembers();
		for (Member member : members) {
			map.put(member.getId(), member);
		}
		return map;
	}

	public static Map<Long, Person> getAllPersonsMap() {
		Map<Long, Person> map = new HashMap<>();
		List<Person> persons = TimeHelper.getAllPersons();
		for (Person person : persons) {
			map.put(person.getId(), person);
		}
		return map;
	}

	public static Map<Long, Customer> getAllCustomersMap() {
		Map<Long, Customer> map = new HashMap<>();
		List<Customer> customers = TimeHelper.getAllCustomers();
		for (Customer customer : customers) {
			map.put(customer.getId(), customer);
		}
		return map;
	}

	public static Map<Long, Project> getAllProjectsMap() {
		Map<Long, Project> map = new HashMap<>();
		List<Project> projects = TimeHelper.getAllProjects();
		for (Project project : projects) {
			map.put(project.getId(), project);
		}
		return map;
	}

	public static void setDay(Date date, Calendar newDay) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.YEAR, newDay.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, newDay.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, newDay.get(Calendar.DAY_OF_MONTH));
		
		date.setTime(calendar.getTime().getTime());
	}

	public static String getTimeString(Date value) {
		return new SimpleDateFormat(FORMAT_TIME).format(value);
	}

	public static Clock timeFromString(String value) throws DateFormatException {
		return Clock.parse(value);
	}

	public static void setClock(Date date, Clock clock) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, clock.getHours());
		calendar.set(Calendar.MINUTE, clock.getMinutes());
		
		date.setTime(calendar.getTime().getTime());
	}

	public static Date getDate(Date date, Clock clock) {
		Date result = new Date(date.getTime());
		setClock(result, clock);
		return result;
	}
	
	public static Period getMemberPeriod(Member member) {
		Calendar start = Period.getFirstDayOfYear();
		Calendar end = Period.getLastDayOfYear();
		
		Project project = null;
		if (member.getStart() != null) {
			start.setTime(member.getStart());
		} else {
			project = getProjectFromMemberId(member.getId());
			if (project.getStart() != null) {
				start.setTime(project.getStart());
			}
		}
		if (member.getEnd() != null) {
			end.setTime(member.getEnd());
		} else {
			if (project == null) {
				project = getProjectFromMemberId(member.getId());
			}
			if (project.getEnd() != null) {
				end.setTime(project.getEnd());
			}
		}
		Period.setToBeginOfDay(start);;
		Period.setToBeginOfDay(end);
		end.add(Calendar.DAY_OF_MONTH, 1);
		return new Period(start, end);
	}

	public static boolean hasTimeEntries(Member member) throws SQLException, InitProfileException {
		Time time = new Time();
		time.setMemberId(member.getId());
		return isExist(time);
	}

	public static boolean isExist(Object bean) throws SQLException, InitProfileException {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.exists(bean);
		}
	}

	public static void delete(Object bean) throws SQLException, InitProfileException {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			dbContext.delete(bean);
		}
	}

	public static boolean hasMembers(Project project) throws SQLException, InitProfileException {
		Member member = new Member();
		member.setProjectId(project.getId());
		return isExist(member);
	}

	public static boolean hasProjects(Customer customer) throws SQLException, InitProfileException {
		Project project = new Project();
		project.setCustomerId(customer.getId());
		return isExist(project);
	}

}
