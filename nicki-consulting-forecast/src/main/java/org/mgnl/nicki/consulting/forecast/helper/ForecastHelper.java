package org.mgnl.nicki.consulting.forecast.helper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mgnl.nicki.consulting.core.helper.Clock;
import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.helper.DateFormatException;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.forecast.model.ForecastCategory;
import org.mgnl.nicki.consulting.forecast.model.ForecastCustomer;
import org.mgnl.nicki.consulting.forecast.model.ForecastDeal;
import org.mgnl.nicki.consulting.forecast.model.ForecastSales;
import org.mgnl.nicki.consulting.forecast.model.ForecastStage;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.core.helper.NameValue;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;

import com.vaadin.flow.component.grid.Grid;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForecastHelper {
	public final static String FORMAT_TIME = "HH:mm";

	public static Object getDealDisplayName(Long dealId) {
		StringBuilder sb = new StringBuilder();
		ForecastDeal deal = getDeal(dealId);
		if (deal != null) {
			ForecastCustomer customer = getCustomer(deal.getCustomerId());
			if (customer != null) {
				sb.append(customer.getName()).append(" - ");
			}
			sb.append(deal.getName());
		}
		if (sb.length() == 0) {
			sb.append("invalid project id:").append(dealId);
		}
		return sb.toString();
	}

	public static ForecastDeal getDeal(Long id) {
		ForecastDeal deal = new ForecastDeal();
		deal.setId(id);
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(deal, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load project", e);
		}
		return null;
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
	
	public static String formatHours(float hours) {

        return String.format("%.2f",hours);
	}


	
	public static ForecastCustomer getCustomer(Long id) {
		ForecastCustomer customer = new ForecastCustomer();
		if (id != null) {
			customer.setId(id);
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(customer, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customer", e);
		}
		return null;
	}
	
	public static ForecastStage getStage(Long id) {
		ForecastStage stage = new ForecastStage();
		if (id != null) {
			stage.setId(id);
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(stage, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load stage", e);
		}
		return null;
	}
	
	public static String toDate(LocalDate date) {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.toDate(DataHelper.getDate(date));
		} catch (SQLException e) {
			log.error("Error generating toDate function", e);
		}
		return null;
	}
	
	public static String toTimestamp(LocalDate date) {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.toTimestamp(DataHelper.getDate(date));
		} catch (SQLException e) {
			log.error("Error generating toDate function", e);
		}
		return null;
	}
	
	public static String toDate(Date date) {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.toDate(date);
		} catch (SQLException e) {
			log.error("Error generating toDate function", e);
		}
		return null;
	}
	
	public static ForecastSales getSales(Long id) {
		ForecastSales sales = new ForecastSales();
		if (id != null) {
			sales.setId(id);
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(sales, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load sales", e);
		}
		return null;
	}
	
	public static List<ForecastCustomer> getAllCustomers() {
		ForecastCustomer customer = new ForecastCustomer();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(customer, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
	
	public static List<ForecastStage> getAllStages() {
		ForecastStage stage = new ForecastStage();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(stage, false, null, "POSITION");
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
	
	public static List<ForecastSales> getAllSales() {
		ForecastSales sales = new ForecastSales();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(sales, false, null, "NAME");
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
	
	public static List<ForecastCategory> getAllCategories() {
		ForecastCategory category = new ForecastCategory();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(category, false, null, "NAME");
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customer", e);
		}
		return new ArrayList<>();
	}
	
	public static List<ForecastDeal> getAllDeals() {
		return getDeals(null);
	}

	public static List<ForecastDeal> getDeals(ForecastCustomer customer) {
		ForecastDeal deal = new ForecastDeal();
		if (customer != null) {
			deal.setCustomerId(customer.getId());
		}
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObjects(deal, true, "VALID_TO IS NULL", "CUSTOMER_ID");
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load deals", e);
		}
		return new ArrayList<>();
	}

	public static List<ForecastDeal> getAllActiveDeals(String filter) {
		ForecastDeal deal = new ForecastDeal();
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			List<ForecastDeal> deals = dbContext.loadObjects(deal, true, filter, "CUSTOMER_ID");
			if (deals != null) {
				return deals;
			}
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load deals", e);
		}
		return new ArrayList<>();
	}

	public static Map<Long, ForecastCustomer> getAllCustomersMap() {
		Map<Long, ForecastCustomer> map = new HashMap<>();
		List<ForecastCustomer> customers = getAllCustomers();
		for (ForecastCustomer customer : customers) {
			map.put(customer.getId(), customer);
		}
		return map;
	}

	public static Map<Long, ForecastDeal> getAllDealssMap() {
		Map<Long, ForecastDeal> map = new HashMap<>();
		List<ForecastDeal> projects = getAllDeals();
		for (ForecastDeal project : projects) {
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

	public static boolean hasDeals(ForecastCustomer customer) throws SQLException, InitProfileException {
		ForecastDeal deal = new ForecastDeal();
		deal.setCustomerId(customer.getId());
		return isExist(deal);
	}

	public static boolean hasDealsWithStage(ForecastStage stage) throws SQLException, InitProfileException {
		ForecastDeal deal = new ForecastDeal();
		deal.setStageId(stage.getId());
		return isExist(deal);
	}

	public static boolean hasDealsWithSales(ForecastSales sales) throws SQLException, InitProfileException {
		ForecastDeal deal = new ForecastDeal();
		deal.setSalesId(sales.getId());
		return isExist(deal);
	}

	public static boolean hasDealsWithCategory(ForecastCategory category) throws SQLException, InitProfileException {
		ForecastDeal deal = new ForecastDeal();
		deal.setCategoryId(category.getId());
		return isExist(deal);
	}
	
	public static Grid<NameValue> getDetails(List<NameValue> values) {
		
		Grid<NameValue> detailsGrid = new Grid<>();
		detailsGrid.addColumn(nv -> nv.getName());
		detailsGrid.addColumn(nv -> nv.getValue());
		detailsGrid.setItems(values);
		detailsGrid.setAllRowsVisible(true);
		
		return detailsGrid;

	}

}
