package org.mgnl.nicki.consulting.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.helper.Clock;
import org.mgnl.nicki.consulting.core.helper.DateFormatException;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Pause;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.views.BaseView.READONLY;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.vaadin.base.data.DateHelper;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeWrapper implements Serializable, Comparable<TimeWrapper> {
	private static final long serialVersionUID = -3445248806029063825L;
	private Person person;
	private Time time;
	private int uniqueHash;
	
	private List<Member> members;
	private Map<Long, Member> membersMap = new HashMap<>();
	
	private ComboBox<Member> memberComboBox;
	private Checkbox deleteCheckBox;
	private Component customerReportComponent;
	private DatePicker dayDateField;
	private TextField startTextField;
	private TextField endTextField;
	private ComboBox<Pause> pauseComboBox;
	private TextField textTextField;
	private Label personLabel;
	private boolean readOnly;
	
	public TimeWrapper(Person person, Time time, List<Member> members, READONLY readonly) {
		super();
		this.person = person;
		this.time = time;
		uniqueHash = time.getUniqueHash();
		this.members = members;
		if (members != null) {
			for (Member member: members) {
				membersMap.put(member.getId(), member);
			}
		}
		this.readOnly = (readonly == READONLY.TRUE);
		if (time.getInvoiceId() != null) {
			this.readOnly = true;
		}
	}
	
	public Checkbox getDelete() {
		if (this.deleteCheckBox == null) {
			this.deleteCheckBox = new Checkbox();
			this.deleteCheckBox.setWidth("-1px");
			if (this.readOnly) {
				this.deleteCheckBox.setEnabled(false);
			}
		}
		return this.deleteCheckBox;

	}
	public boolean isDelete() {
		if (getDelete() != null) {
			return getDelete().getValue().booleanValue();
		} else {
			return false;
		}
	}
	
	public Component getCustomerReport() {
		if (this.customerReportComponent == null) {
			if (disableCustomerReport()) {
				this.customerReportComponent = new Label(" ");
			} else {
				Checkbox customerReportCheckBox = new Checkbox();
				customerReportCheckBox.setValue(false);
				customerReportCheckBox.addValueChangeListener(
						event -> time.setCustomerReport(customerReportCheckBox.getValue()));
				if (time.getCustomerReport() != null) {
					customerReportCheckBox.setValue(time.getCustomerReport());
				} else {
					time.setCustomerReport(false);
				}
				if (disableCustomerReport()) {
					customerReportCheckBox.setValue(false);
					customerReportCheckBox.setReadOnly(true);
				}
				if (this.readOnly) {
					customerReportCheckBox.setReadOnly(true);
				}
				this.customerReportComponent = customerReportCheckBox;
			}
		}
		return this.customerReportComponent;

	}
	
	public String getCustomerReportFlag() {
		if (!disableCustomerReport()) {
			return time.getCustomerReport() ? "X" : "";
		} else {
			return "";
		}
	}
	
	public boolean disableCustomerReport() {
		Project project = getProject();
		if (project == null) {
			return false;
		}
		if (project.getCustomerReport() == null) {
			return true;
		}
		if (project.getCustomerReport() == Boolean.TRUE) {
			return false;
		}
		return true;
	}

	public Project getProject() {
		if (memberComboBox.getValue() != null) {
			Member member = (Member) memberComboBox.getValue();
			return TimeHelper.getProject(member.getProjectId());
		}
		return null;
	}
	
	public ComboBox<Member> getMember() {
		if (this.memberComboBox == null) {
			memberComboBox = new ComboBox<>();
			log.debug("Members: " + members);
			memberComboBox.setItems(members);
			memberComboBox.setWidth("100%");
			memberComboBox.setItemLabelGenerator(Member::getDisplayName);
			
			if (time.getMemberId() != null) {
				log.debug("MemberId: " + time.getMemberId());
				for (Member member : members) {
					log.debug("Test Member: " + member);
					if (time.getMemberId().longValue() == member.getId().longValue()) {
						memberComboBox.setValue(member);
						log.debug("Selected: " + member);
					} else {
						log.debug("not maching: " + time.getMemberId() + "<-->" + member.getId());
					}
				}
			}
			memberComboBox.addValueChangeListener(event -> {
				Member m = event.getValue();
				if (m != null) {
					time.setMemberId(m.getId());
				} else {
					time.setMemberId(null);
				}
			});
			
			memberComboBox.setClearButtonVisible(true);

			memberComboBox.setReadOnly(readOnly);
		}
		return memberComboBox;
	}
	
	public String getMemberDisplayName() {
		if (time.getMemberId() != null) {
			for (Member member : members) {
				if (time.getMemberId().longValue() == member.getId().longValue()) {
					return member.getDisplayName();
				}
			}
		}
		return "";
	}
	
	public String getMemberName() {
		return getMember(time.getMemberId()).getDisplayName();
	}
	
	public DatePicker getDay() {
		if (this.dayDateField == null) {
			dayDateField = new DatePicker();
			DateHelper.init(dayDateField);
			
			if (time.getStart() != null) {
				dayDateField.setValue(DataHelper.getLocalDate(time.getStart()));
			}
			dayDateField.addValueChangeListener(event -> {
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.ERROR_MESSAGE);
				}
			});
			
			dayDateField.setReadOnly(readOnly);
		}
		return dayDateField;
	}
	public String getDisplayDay() {
		return DataHelper.getDisplayDay(time.getStart());
	}

	public TextField getStart() {
		if (this.startTextField == null) {
			startTextField = new TextField();
			startTextField.setWidth("100%");
			
			if (time.getStart() != null) {
				startTextField.setValue(TimeHelper.getTimeString(time.getStart()));
			}
			
			startTextField.addValueChangeListener(event -> {
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.WARNING_MESSAGE);
				}
			});
			
			startTextField.setReadOnly(readOnly);
		}
		return startTextField;
	}
	public String getDisplayStart() {
		return TimeHelper.getTimeString(time.getStart());
	}
	
	protected void timeChanged() throws DateFormatException {
		if (getDay().getValue() != null) {
			if (StringUtils.isNotBlank(getStart().getValue())) {
				String startEntry = StringUtils.stripToEmpty(getStart().getValue());
				time.setStart(TimeHelper.getDate(DataHelper.getDate(getDay().getValue()), Clock.parse(startEntry)));
			} else {
				time.setStart(null);
			}
			if (StringUtils.isNotBlank(getEnd().getValue())) {
				String endEntry = StringUtils.stripToEmpty(getEnd().getValue());
				time.setEnd(TimeHelper.getDate(DataHelper.getDate(getDay().getValue()), Clock.parse(endEntry)));
			} else {
				time.setEnd(null);
			}
		} else {
			time.setStart(null);
			time.setEnd(null);			
		}
		if (log.isDebugEnabled()) {
			Notification.show("" + time.getStart() + " - " + time.getEnd(), Type.TRAY_NOTIFICATION);
		}
	}

	public TextField getEnd() {
		if (this.endTextField == null) {
			endTextField = new TextField();
			endTextField.setWidth("100%");
			
			if (time.getEnd() != null) {
				endTextField.setValue(TimeHelper.getTimeString(time.getEnd()));
			}
			
			endTextField.addValueChangeListener(event -> {
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.WARNING_MESSAGE);
				}
			});
			
			endTextField.setReadOnly(readOnly);
		}
		return endTextField;
	}
	public String getDisplayEnd() {
		return TimeHelper.getTimeString(time.getEnd());
	}
	
	public String getHours() {
		if (time.getHours() != null) {
			return TimeHelper.formatHours(time.getHours());
		} else {
			return "";
		}
	}
	
	public Float getHoursAsFloat() {
		if (time.getHours() != null) {
			return time.getHours();
		} else {
			return 0.0f;
		}
	}
	
	public String getWeekDay() {
		if (time.getStart() != null) {
			return TimeHelper.formatWeekDay(time.getStart());
		} else {
			return "";
		}
	}

	public ComboBox<Pause> getPause() {
		if (this.pauseComboBox == null) {
			pauseComboBox = new ComboBox<>();
			pauseComboBox.setWidth("-1px");
			pauseComboBox.setItems(Pause.values());
			pauseComboBox.setItemLabelGenerator(Pause::getDisplayName);
			
			if (time.getPause() != null) {
				pauseComboBox.setValue(Pause.getPause(time.getPause()));
			}
			
			pauseComboBox.addValueChangeListener(event -> {
				if (pauseComboBox.getValue() != null) {
					this.time.setPause( pauseComboBox.getValue().getMins());
				} else {
					this.time.setPause(null);
				}
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.ERROR_MESSAGE);
				}
			});
			pauseComboBox.setClearButtonVisible(true);
			pauseComboBox.setReadOnly(readOnly);
		}
		return pauseComboBox;
	}
	
	public String getDisplayPause() {
		if (time.getPause() == null || Pause.getPause(time.getPause()) == null) {
			return "";
		} else {
			return Pause.getPause(time.getPause()).getDisplayName();
		}
	}

	public TextField getText() {
		if (this.textTextField == null) {
			textTextField = new TextField();
			textTextField.setWidth("100%");
			
			if (time.getText() != null) {
				textTextField.setValue(time.getText());
			}
			
			textTextField.addValueChangeListener(event -> {
				if (textTextField.getValue() != null) {
					this.time.setText(StringUtils.stripToNull(textTextField.getValue()));
				} else {
					this.time.setText(null);
				}
			});
			
			textTextField.setReadOnly(readOnly);
		}
		return textTextField;
	}

	public String getTextString() {
		return time.getText();
	}

	public Label getPerson() {
		if (this.person != null) {
			personLabel = new Label();
			
			if (time.getText() != null) {
				personLabel.setText(getPersonName());
			}
		}
		return personLabel;
	}
	
	public String getPersonName() {
		return person.getName();
	}

	public Time getTime() {
		return time;
	}
	
	public boolean isModified() {
		return time.getUniqueHash() != uniqueHash;
	}
	
	public boolean isMarkedDelete() {
		return deleteCheckBox != null && deleteCheckBox.getValue();
	}

	public void setMessages(List<String> messages) {
		String html = getHtml(messages);
		setErrorMessage(this.memberComboBox, html);
		setErrorMessage(this.dayDateField, html);
		setErrorMessage(this.startTextField, html);
		setErrorMessage(this.endTextField, html);
		setErrorMessage(this.textTextField, html);
		setErrorMessage(this.pauseComboBox, html);
		
	}

	private void setErrorMessage(HasElement component, String errorMessage) {
		component.getElement().setAttribute("title", errorMessage);
		component.getElement().getClassList().add("error");
	}

	private String getHtml(List<String> messages) {
		if (messages != null && messages.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String message : messages) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(message);
			}
			return sb.toString();
		} else {
			return null;
		}
	}
	
	public Member getMember(Long memberId) {
		return this.membersMap.get(memberId);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isVacation() {
		Project project = getProject();
		return project != null && project.getVacation() != null && project.getVacation();
	}

	@Override
	public int compareTo(TimeWrapper o) {
		if (getTime().getStart() != null) {
			if (o.time.getStart() != null) {
				return getTime().getStart().compareTo(o.getTime().getStart());
			} else {
				return -1;
			}
		} else if (o.getTime().getStart() != null) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMemberName());
		sb.append("; ").append(TimeHelper.getTimeString(time.getStart()));
		sb.append("; ").append(TimeHelper.getTimeString(time.getEnd()));
		if (time.getPause() != null) {
			sb.append("; ").append(Pause.getPause(time.getPause()));
		}
		sb.append("; ").append(TimeHelper.formatHours(time.getHours()));
		sb.append("; ").append(time.getText());
		return sb.toString();
	}
}
