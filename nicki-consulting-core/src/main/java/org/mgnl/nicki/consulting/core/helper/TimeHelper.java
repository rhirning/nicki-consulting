package org.mgnl.nicki.consulting.core.helper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.TimeWrapper;
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
	
	public static float getHoursFromTimeList(List<Time> times) {
		float hours = 0;
		for (Time time : times) {
			hours += time.getHours();
		}
		return hours;
	}
	
	public static float getHoursFromTimeWrapperList(List<TimeWrapper> timeWrappers) {
		float hours = 0;
		for (TimeWrapper timeWrapper : timeWrappers) {
			hours += timeWrapper.getTime().getHours();
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
		customer.setId(id);
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(customer, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load customer", e);
		}
		return null;
	}
	


	public static List<Member> getMembers(Project project) {
		Member member= new Member();
		member.setProjectId(project.getId());
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(member, true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}
		return new ArrayList<>();
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

}
