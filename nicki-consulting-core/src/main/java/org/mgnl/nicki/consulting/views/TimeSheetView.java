package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.helper.VerifyException;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.data.BeanContainerDataSource;
import org.mgnl.nicki.consulting.data.TimeWrapper;
import org.mgnl.nicki.consulting.db.TimeSelectException;
import org.mgnl.nicki.consulting.views.SaveOrIgnoreDialog.DECISION;
import org.mgnl.nicki.core.i18n.I18n;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TimeSheetView extends BaseView implements View {

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private Table timeTable;
	@AutoGenerated
	private HorizontalLayout filterLayout;
	@AutoGenerated
	private ComboBox personComboBox;
	@AutoGenerated
	private ComboBox timeComboBox;
	@AutoGenerated
	private Button reloadButton;
	@AutoGenerated
	private Button saveButton;

	private static final long serialVersionUID = 8329773710892387845L;
	private static final Logger LOG = LoggerFactory.getLogger(TimeSheetView.class);
	private BeanContainerDataSource<TimeWrapper> timeContainerDataSource;
	
	private boolean isInit;
	
	private Window decisionWindow;
	
	public TimeSheetView() {
		buildMainLayout();
		setCompositionRoot(mainLayout);
	}

	@Override
	public void init() {
		if (!isInit) {
			initTimeComboBox(this.timeComboBox);
			timeComboBox.addValueChangeListener(event -> { timeValueChanged(); });
			try {
				initPersonComboBox(this.personComboBox, ALL.FALSE);
			} catch (NoValidPersonException | NoApplicationContextException e) {
				LOG.error("Error init personComboBox", e);
			}
			personComboBox.addValueChangeListener(event -> { timeValueChanged(); });
			timeContainerDataSource = new BeanContainerDataSource<>(TimeWrapper.class);
			timeTable.setContainerDataSource(timeContainerDataSource);
			timeTable.setVisibleColumns("delete", "member", "day", "start", "end", "pause", "hours", "text");
			timeTable.setColumnHeaders("L�schen", "Projekt", "Datum", "von", "bis", "Pause", "Stunden", "T�tigkeit");
//			timeTable.setColumnWidth("member", 400);
//			timeTable.setColumnWidth("day", 100);
//			timeTable.setColumnWidth("start", 100);
//			timeTable.setColumnWidth("end", 100);
			
			saveButton.addClickListener(event-> {save();});
			reloadButton.addClickListener(event -> {loadTimes();});

			isInit = true;
		}
		loadTimes();
	}

	private void timeValueChanged() {
		LOG.debug("timeValueChanged");
		if (decisionWindow != null) {
			LOG.debug("saveOrIgnore: removeWindow");
			UI.getCurrent().removeWindow(decisionWindow);
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
				UI.getCurrent().removeWindow(decisionWindow);
				decisionWindow = null;
			}
		});
		LOG.debug("saveOrIgnore: addWindow");
		decisionWindow = new NonClosingWindow("Daten haben sich ge�ndert", dialog);
		decisionWindow.setModal(true);
		UI.getCurrent().addWindow(decisionWindow);
	}

	private void save() {
		if (!verify()) {
			Notification.show("Es gibt noch Fehler in den markierten Zeilen. Mehr Information bei Mouse-Over", Type.ERROR_MESSAGE);
			timeComboBox.setValue(getTimeComboBoxValue());
			return;
		}

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			for (TimeWrapper timeWrapper : timeContainerDataSource.getItemIds()) {
				if (timeWrapper.isMarkedDelete()) {
					deleteOrIgnore(dbContext, timeWrapper.getTime());
				} else {
					saveOrIgnore(dbContext, timeWrapper.getTime());
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
		for (TimeWrapper timeWrapper : timeContainerDataSource.getItemIds()) {
			try {
				ok &= verify(timeWrapper);
			} catch (VerifyException e) {
				timeWrapper.setMessages(e.getMessages());
				ok = false;
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
		if (time.getStart() != null && !getTimeComboBoxValue().matches(time.getStart())) {
			messages.add(I18n.getText("nicki.consulting.time.error.start.invalid"));
			ok = false;
		}
		if (time.getEnd() == null) {
			messages.add(I18n.getText("nicki.consulting.time.error.end.missing"));
			ok = false;
		} else {
			empty = false;
		}
		if (time.getEnd() != null && !getTimeComboBoxValue().matches(time.getEnd())) {
			messages.add(I18n.getText("nicki.consulting.time.error.end.invalid"));
			ok = false;
		}
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

	private void loadTimes() {
		timeContainerDataSource.removeAllItems();
		try {
			PERIOD period = (PERIOD) timeComboBox.getValue();
			if (period != null) {
				timeContainerDataSource.addAll(getTimeWrappers(getPersonComboBoxValue(), period.getPeriod(), null, null, READONLY.FALSE, 10));
			}
		} catch (IllegalStateException | IllegalArgumentException | TimeSelectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isModified() {
		for (TimeWrapper timeWrapper : timeContainerDataSource.getItemIds()) {
			if (timeWrapper.isModified()) {
				return true;
			}
		}
		return false;
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// filterLayout
		filterLayout = buildFilterLayout();
		mainLayout.addComponent(filterLayout);
		
		// timeTable
		timeTable = new Table();
		timeTable.setImmediate(false);
		timeTable.setWidth("-1px");
		timeTable.setHeight("100.0%");
		mainLayout.addComponent(timeTable);
		mainLayout.setExpandRatio(timeTable, 1.0f);
		
		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildFilterLayout() {
		// common part: create layout
		filterLayout = new HorizontalLayout();
		filterLayout.setImmediate(false);
		filterLayout.setWidth("-1px");
		filterLayout.setHeight("-1px");
		filterLayout.setMargin(false);
		filterLayout.setSpacing(true);
		
		// saveButton
		saveButton = new Button();
		saveButton.setCaption("Speichern");
		saveButton.setImmediate(true);
		saveButton.setWidth("-1px");
		saveButton.setHeight("-1px");
		filterLayout.addComponent(saveButton);
		
		// reloadButton
		reloadButton = new Button();
		reloadButton.setCaption("Verwerfen");
		reloadButton.setImmediate(true);
		reloadButton.setWidth("-1px");
		reloadButton.setHeight("-1px");
		filterLayout.addComponent(reloadButton);
		
		// timeComboBox
		timeComboBox = new ComboBox();
		timeComboBox.setImmediate(true);
		timeComboBox.setWidth("-1px");
		timeComboBox.setHeight("-1px");
		filterLayout.addComponent(timeComboBox);
		
		// personComboBox
		personComboBox = new ComboBox();
		personComboBox.setImmediate(true);
		personComboBox.setWidth("-1px");
		personComboBox.setHeight("-1px");
		filterLayout.addComponent(personComboBox);
		
		return filterLayout;
	}

}
