package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.helper.VerifyException;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.consulting.views.SaveOrIgnoreDialog.DECISION;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.core.i18n.I18n;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class TimeSheetView extends BaseView implements View {
	
	private Grid<TimeWrapper> timeTable;
	
	private HorizontalLayout filterLayout;
	
	private Select<Person> personComboBox;
	
	private Select<PERIOD> timeComboBox;
	
	private Button reloadButton;
	
	private Button saveButton;
	
	private Button moreLinesButton;

	private static final long serialVersionUID = 8329773710892387845L;
	private static final Logger LOG = LoggerFactory.getLogger(TimeSheetView.class);

	private List<TimeWrapper> times = new ArrayList<>();
	
	private boolean isInit;
	
	private Dialog decisionWindow;
		
	public TimeSheetView() {
		buildMainLayout();
	}

	@Override
	public void init() {
		if (!isInit) {
//			timeTable.setStyleName("mygridwithcomponents");
			initTimeComboBox(this.timeComboBox);
			timeComboBox.addValueChangeListener(event -> { timeValueChanged(); });
			try {
				initPersonComboBox(this.personComboBox, ALL.FALSE);
			} catch (NoValidPersonException | NoApplicationContextException e) {
				LOG.error("Error init personComboBox", e);
			}
			personComboBox.addValueChangeListener(event -> { timeValueChanged(); });
			
			saveButton.addClickListener(event-> {save();});
			reloadButton.addClickListener(event -> {loadTimes();});
			moreLinesButton.addClickListener(event -> {
				addLines((10));
			});
			
			Icon deleteIcon = createIcon(VaadinIcon.TRASH, "Löschen");
			deleteIcon.addClickListener(event -> setDeleteFlags());
			
			Icon customerReportIcon = createIcon(VaadinIcon.FILE, "Bei Kunde erfasst");
			customerReportIcon.addClickListener(event -> setCustomerReportFlags());
			
			timeTable.addComponentColumn(TimeWrapper::getDelete).setHeader(deleteIcon).setFlexGrow(0).setWidth("50px");
			timeTable.addComponentColumn(TimeWrapper::getMember).setHeader("Projekt").setWidth("300px").setFlexGrow(1);
			timeTable.addComponentColumn(TimeWrapper::getDay).setHeader("Datum").setFlexGrow(0).setWidth("210px");
			timeTable.addComponentColumn(TimeWrapper::getStart).setHeader("von").setFlexGrow(0);//.setWidth("40px");
			timeTable.addComponentColumn(TimeWrapper::getEnd).setHeader("bis").setFlexGrow(0);//.setWidth("40px");
			timeTable.addComponentColumn(TimeWrapper::getPause).setHeader("Pause").setFlexGrow(0).setWidth("180px");
			timeTable.addColumn(TimeWrapper::getHours).setHeader("Stunden").setFlexGrow(0);//.setWidth("50px");
			timeTable.addComponentColumn(TimeWrapper::getCustomerReport).setHeader(customerReportIcon).setFlexGrow(0).setWidth("50px");
			timeTable.addComponentColumn(TimeWrapper::getText).setHeader("Tätigkeit").setWidth("200px").setFlexGrow(1);
			//timeTable.setHeightByRows(true);
			timeTable.setHeight("100%");
			timeTable.setItemDetailsRenderer(new ComponentRenderer<>(t -> getDetails(t)));
			setFlexGrow(1, timeTable);
			isInit = true;
		}
		loadTimes();
	}
	
	private void setCustomerReportFlags() {
		for (TimeWrapper entry : times) {
			Time time = entry.getTime();
			if (time.getStart() != null &&!entry.isReadOnly() &&  entry.getCustomerReport() instanceof Checkbox) {
				Checkbox checkbox = (Checkbox) entry.getCustomerReport();
				checkbox.setValue(!checkbox.getValue());
			}
		}
	}
	
	private void setDeleteFlags() {
		for (TimeWrapper entry : times) {
			Time time = entry.getTime();
			if (time.getStart() != null && !entry.isReadOnly()) {
				entry.getDelete().setValue(!entry.getDelete().getValue());
			}
		}
	}

	private Component getDetails(TimeWrapper timeWrapper) {
		if (!timeWrapper.isReadOnly()) {
			HorizontalLayout layout = new HorizontalLayout();
			DatePicker fromDatePicker = new DatePicker("von");
			fromDatePicker.setValue(DataHelper.getLocalDate(getFirstDayInMonth(timeWrapper.getTime().getStart()).getTime()));
			DatePicker toDatePicker = new DatePicker("bis");
			toDatePicker.setValue(DataHelper.getLocalDate(getLastDayInMonth(timeWrapper.getTime().getStart()).getTime()));
			Button button = new Button("kopieren");
			button.addClickListener(event -> {
				fillMonth(timeWrapper, fromDatePicker.getValue(), toDatePicker.getValue());
			});
			layout.add(fromDatePicker, toDatePicker, button);
			layout.setAlignItems(Alignment.END);
			return layout;
		} else {
			return new Span("");
		}
	}

	private void fillMonth(TimeWrapper timeWrapper, LocalDate from, LocalDate to) {
		try {
			verify(timeWrapper);
		} catch (VerifyException e) {
			timeWrapper.setMessages(e.getMessages());
			Notification.show("Es gibt noch Fehler in den markierten Zeilen. Mehr Information bei Mouse-Over", Type.ERROR_MESSAGE);
			return;
		}
		
		if (StringUtils.isBlank(timeWrapper.getTime().getText())) {
			Notification.show("Bitte einen ausgefüllten Eintrag als Vorlage nehmen");
			return;
		}
		
		addLines(30);

		
		// alle Wochentage des Monats
		Calendar first= Calendar.getInstance();
		first.setTime(DataHelper.getDate(from));
		
		Calendar nextFirst= Calendar.getInstance();
		nextFirst.setTime(DataHelper.getDate(to));
		nextFirst.add(Calendar.DAY_OF_MONTH, 1);
		
		while (first.before(nextFirst)) {
			if (!weekend(first) && !existsEntry(first)) {
				for (TimeWrapper entry : times) {
					Time time = entry.getTime();
					if (time.getStart() == null) {
						fillEntry(entry, timeWrapper, first);
						break;
					}
				}
			}
			first.add(Calendar.DAY_OF_MONTH, 1);
		}
		Notification.show("So jetzt würde ich den Monat ausfüllen");
	}
	
	private Calendar getFirstDayInMonth(Date date) {
		Calendar first= Calendar.getInstance();
		if (date != null) {
			first.setTime(date);
			
			first.add(Calendar.DAY_OF_MONTH, -1 * (first.get(Calendar.DAY_OF_MONTH) - 1));
		}
		return first;
	}
	
	private Calendar getLastDayInMonth(Date date) {
		Calendar last= getFirstDayInMonth(date);
		
		last.add(Calendar.MONTH, 1);
		last.add(Calendar.DAY_OF_MONTH, -1);
		return last;
	}

	private void fillEntry(TimeWrapper time, TimeWrapper from, Calendar day) {
		time.getMember().setValue(from.getMember().getValue());
		time.getDay().setValue(DataHelper.getLocalDate(day.getTime()));
		time.getStart().setValue(from.getStart().getValue());
		time.getEnd().setValue(from.getEnd().getValue());
		time.getPause().setValue(from.getPause().getValue());
		time.getText().setValue(from.getText().getValue());
	}

	private boolean weekend(Calendar cal) {
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
	}

	private boolean existsEntry(Calendar first) {
		for (TimeWrapper timeWrapper : times) {
			Time time = timeWrapper.getTime();
			if (time.getStart() != null && isSameDay(first.getTime(), time.getStart())) {
				return true;
			}
		}
		return false;
	}

	private boolean isSameDay(Date date1, Date date2) {
		return StringUtils.equals(DataHelper.getDay(date1), DataHelper.getDay(date2));
	}

	private void timeValueChanged() {
		LOG.debug("timeValueChanged");
		if (decisionWindow != null) {
			LOG.debug("saveOrIgnore: removeWindow");
			decisionWindow.close();
			decisionWindow = null;
		}

		if (isModified()) {
			showSaveOrIgnore();
		} else {
			setTimeComboBoxValue((PERIOD) timeComboBox.getValue());
			setPersonComboBoxValue((Person) this.personComboBox.getValue());
			loadTimes();
		}
	}
	
	private void showSaveOrIgnore() {
		LOG.debug("saveOrIgnore");
		SaveOrIgnoreDialog dialog = new SaveOrIgnoreDialog(result -> {
			if (result == DECISION.SAVE) {
				LOG.debug("saveOrIgnore: decision save");
				save();
			} else {
				LOG.debug("saveOrIgnore: decision ignore");
				setTimeComboBoxValue((PERIOD) timeComboBox.getValue());
				loadTimes();
			}
			if (decisionWindow != null) {
				LOG.debug("saveOrIgnore: removeWindow");
				decisionWindow.close();
				decisionWindow = null;
			}
		});
		LOG.debug("saveOrIgnore: addWindow");
		decisionWindow = new NonClosingWindow("Daten haben sich geändert", dialog);
		decisionWindow.setModal(true);
		decisionWindow.open();
	}

	private void save() {
		if (!verify()) {
			Notification.show("Es gibt noch Fehler in den markierten Zeilen. Mehr Information bei Mouse-Over", Type.ERROR_MESSAGE);
			timeComboBox.setValue(getTimeComboBoxValue());
			return;
		}

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			for (TimeWrapper timeWrapper : times) {
				if (!timeWrapper.isReadOnly()) {
					if (timeWrapper.isMarkedDelete()) {
						deleteOrIgnore(dbContext, timeWrapper.getTime());
					} else {
						saveOrIgnore(dbContext, timeWrapper.getTime());
					}
				}
			}
			setTimeComboBoxValue((PERIOD) timeComboBox.getValue());
			loadTimes();
			Notification.show("Die Daten wurden gespeichert", Type.HUMANIZED_MESSAGE);
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			LOG.error("Could not save times", e);
			Notification.show("Die Daten konnten nicht gespeichert werden: " + e.getMessage(), Type.ERROR_MESSAGE);
		}
	}

	private void deleteOrIgnore(DBContext dbContext, Time time) throws NotSupportedException, SQLException, InitProfileException {
		if (time.getId() != null) {
			dbContext.delete(time);
		}
	}

	private void saveOrIgnore(DBContext dbContext, Time time) throws NotSupportedException, SQLException, InitProfileException {
		if (time.getId() != null) {
			time.setHours(getHours(time));
			dbContext.update(time);
		} else if (time.getStart() != null) {
			time.setHours(getHours(time));
			dbContext.create(time);
		}
	}

	private Float getHours(Time time) {
		// calculate hours
		long millis = time.getEnd().getTime() - time.getStart().getTime();
		long mins = Math.round(millis/(1000.0 * 60));
		if (time.getPause() != null) {
			mins -= time.getPause();
		}
		return mins / 60.0f;
	}

	private boolean verify() {
		boolean ok = true;
		for (TimeWrapper timeWrapper : times) {
			if (!timeWrapper.isReadOnly()) {
				try {
					ok &= verify(timeWrapper);
				} catch (VerifyException e) {
					timeWrapper.setMessages(e.getMessages());
					ok = false;
				}
			}
		}
		return ok;
	}

	private boolean verify(TimeWrapper timeWrapper) throws VerifyException {
		Time time = timeWrapper.getTime();
		if (timeWrapper.isMarkedDelete()) {
			return true;
		}
		boolean empty = true;
		boolean ok = true;
		List<String> messages = new ArrayList<>();
		if (time.getMemberId() == null) {
			ok = false;
			messages.add(I18n.getText("nicki.consulting.time.error.memberid.missing"));
		} else {
			empty = false;
		}
		if (StringUtils.isBlank(time.getText())) {
			ok = false;
			messages.add(I18n.getText("nicki.consulting.time.error.text.missing"));
		} else {
			empty = false;
		}
		if (time.getStart() == null) {
			messages.add(I18n.getText("nicki.consulting.time.error.start.missing"));
			ok = false;
		} else {
			empty = false;
		}
//		if (time.getStart() != null && !getTimeComboBoxValue().matches(time.getStart())) {
//			messages.add(I18n.getText("nicki.consulting.time.error.start.invalid"));
//			ok = false;
//		}
		if (time.getEnd() == null) {
			messages.add(I18n.getText("nicki.consulting.time.error.end.missing"));
			ok = false;
		} else {
			empty = false;
		}
//		if (time.getEnd() != null && !getTimeComboBoxValue().matches(time.getEnd())) {
//			messages.add(I18n.getText("nicki.consulting.time.error.end.invalid"));
//			ok = false;
//		}
		if (time.getStart() != null && time.getEnd() != null &&  time.getStart().after(time.getEnd())) {
			messages.add(I18n.getText("nicki.consulting.time.error.start.end.invalid"));
			ok = false;
		}
		
		if (time.getMemberId() != null && time.getStart() != null) {
			if (!TimeHelper.getMemberPeriod(timeWrapper.getMember(time.getMemberId())).matches(time.getStart())) {
				messages.add(I18n.getText("nicki.consulting.time.error.period.invalid"));
				ok = false;
			}
		}
		if  (!empty && !ok) {
			throw new VerifyException(messages);
		}
		return ok || empty;
	}
	
	private void addLines(int emptyCount) {
		PERIOD period = (PERIOD) timeComboBox.getValue();
		if (period != null) {
			try {
				addEmptyTimeWrappers(times, getPersonComboBoxValue(), period.getPeriod(), READONLY.FALSE, emptyCount);
				timeTable.setItems(times);
			} catch (TimeSelectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadTimes() {
		try {
			PERIOD period = (PERIOD) timeComboBox.getValue();
			if (period != null) {
				times = getTimeWrappers(getPersonComboBoxValue(), period.getPeriod(), null, null, READONLY.FALSE, 10);
				timeTable.setItems(times);
			}
		} catch (IllegalStateException | IllegalArgumentException | TimeSelectException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isModified() {
		for (TimeWrapper timeWrapper : times) {
			if (timeWrapper.isModified()) {
				return true;
			}
		}
		return false;
	}
	
	private Grid<TimeWrapper> createTimeTable() {

		if (timeTable != null) {
			remove(timeTable);
		}
		
		// timeTable
		timeTable = new Grid<TimeWrapper>();
//		timeTable.setWidthFull();
		add(timeTable);
		//setFlexGrow(1, timeTable);
		return timeTable;
	}

	
	private void buildMainLayout() {
		setWidth("-1px");
		setHeight("100%");
		
		// filterLayout
		filterLayout = buildFilterLayout();
		add(filterLayout);

		createTimeTable();
	}

	
	private HorizontalLayout buildFilterLayout() {
		// common part: create layout
		filterLayout = new HorizontalLayout();
		filterLayout.setWidth("-1px");
		filterLayout.setHeight("-1px");
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		
		// saveButton
		saveButton = new Button();
		saveButton.setText("Speichern");
		saveButton.setWidth("-1px");
		saveButton.setHeight("-1px");
		filterLayout.add(saveButton);
		
		// reloadButton
		reloadButton = new Button();
		reloadButton.setText("Verwerfen");
		reloadButton.setWidth("-1px");
		reloadButton.setHeight("-1px");
		filterLayout.add(reloadButton);
		
		// moreLinesButton
		moreLinesButton = new Button();
		moreLinesButton.setText("Mehr Zeilen");
		moreLinesButton.setWidth("-1px");
		moreLinesButton.setHeight("-1px");
		filterLayout.add(moreLinesButton);
		
		// timeComboBox
		timeComboBox = new Select<PERIOD>();
		timeComboBox.setWidth("-1px");
		timeComboBox.setHeight("-1px");
		filterLayout.add(timeComboBox);
		
		// personComboBox
		personComboBox = new Select<Person>();
		personComboBox.setWidth("-1px");
		personComboBox.setHeight("-1px");
		filterLayout.add(personComboBox);
		
		return filterLayout;
	}

}
