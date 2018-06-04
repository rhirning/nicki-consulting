package org.mgnl.nicki.consulting.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.Clock;
import org.mgnl.nicki.consulting.core.helper.DateFormatException;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.views.BaseView.READONLY;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;

public class TimeWrapper implements Serializable {

	private static final long serialVersionUID = -3445248806029063825L;
	private Person person;
	private Time time;
	private int uniqueHash;
	
	private List<Member> members;
	private Map<Long, Member> membersMap = new HashMap<>();
	
	private ComboBox memberComboBox;
	private CheckBox deleteCheckBox;
	private Component customerReportComponent;
	private DateField dayDateField;
	private TextField startTextField;
	private TextField endTextField;
	private ComboBox pauseComboBox;
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
	
	public CheckBox getDelete() {
		if (this.deleteCheckBox == null) {
			this.deleteCheckBox = new CheckBox();
			if (this.readOnly) {
				this.deleteCheckBox.setEnabled(false);
			}
		}
		return this.deleteCheckBox;

	}
	
	public Component getCustomerReport() {
		if (this.customerReportComponent == null) {
			if (disableCustomerReport()) {
				this.customerReportComponent = new Label(" ");
			} else {
				CheckBox customerReportCheckBox = new CheckBox();
				customerReportCheckBox.setImmediate(true);
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
	
	public ComboBox getMember() {
		if (this.memberComboBox == null) {
			memberComboBox = new ComboBox();
			for (Member member : members) {
				memberComboBox.addItem(member);
				memberComboBox.setItemCaption(member, member.getDisplayName());
			}
			
			if (time.getMemberId() != null) {
				for (Member member : members) {
					if (time.getMemberId() == member.getId()) {
						memberComboBox.select(member);
					}
				}
			}
			memberComboBox.addValueChangeListener(event -> {
				Member m = (Member) event.getProperty().getValue();
				if (m != null) {
					time.setMemberId(m.getId());
				} else {
					time.setMemberId(null);
				}
			});
			memberComboBox.setReadOnly(readOnly);
		}
		return memberComboBox;
	}
	
	public DateField getDay() {
		if (this.dayDateField == null) {
			dayDateField = new DateField();
			dayDateField.setDateFormat("dd.MM.yy");
			
			if (time.getStart() != null) {
				dayDateField.setValue(time.getStart());
			}
			dayDateField.setImmediate(true);
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

	public TextField getStart() {
		if (this.startTextField == null) {
			startTextField = new TextField();
			startTextField.setWidth("50px");
			startTextField.setImmediate(true);
			
			if (time.getStart() != null) {
				startTextField.setValue(TimeHelper.getTimeString(time.getStart()));
			}
			
			startTextField.addValueChangeListener(event -> {
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.ERROR_MESSAGE);
				}
			});
			
			startTextField.setReadOnly(readOnly);
		}
		return startTextField;
	}
	
	protected void timeChanged() throws DateFormatException {
		if (dayDateField.getValue() != null) {
			if (StringUtils.isNotBlank(startTextField.getValue())) {
				String startEntry = StringUtils.stripToEmpty(startTextField.getValue());
				time.setStart(TimeHelper.getDate(dayDateField.getValue(), Clock.parse(startEntry)));
			} else {
				time.setStart(null);
			}
			if (StringUtils.isNotBlank(endTextField.getValue())) {
				String endEntry = StringUtils.stripToEmpty(endTextField.getValue());
				time.setEnd(TimeHelper.getDate(dayDateField.getValue(), Clock.parse(endEntry)));
			} else {
				time.setEnd(null);
			}
		} else {
			time.setStart(null);
			time.setEnd(null);			
		}
		Notification.show("" + time.getStart() + " - " + time.getEnd(), Type.TRAY_NOTIFICATION);
	}

	public TextField getEnd() {
		if (this.endTextField == null) {
			endTextField = new TextField();
			endTextField.setWidth("50px");
			endTextField.setImmediate(true);
			
			if (time.getEnd() != null) {
				endTextField.setValue(TimeHelper.getTimeString(time.getEnd()));
			}
			
			endTextField.addValueChangeListener(event -> {
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.ERROR_MESSAGE);
				}
			});
			
			endTextField.setReadOnly(readOnly);
		}
		return endTextField;
	}
	
	public String getHours() {
		if (time.getHours() != null) {
			return TimeHelper.formatHours(time.getHours());
		} else {
			return "";
		}
	}

	public ComboBox getPause() {
		if (this.pauseComboBox == null) {
			pauseComboBox = new ComboBox();
			pauseComboBox.setWidth("100px");
			pauseComboBox.setImmediate(true);
			pauseComboBox.addItem(30);
			pauseComboBox.setItemCaption(30, "30 Min.");
			pauseComboBox.addItem(60);
			pauseComboBox.setItemCaption(60, "60 Min.");
			
			if (time.getPause() != null) {
				pauseComboBox.setValue(time.getPause());
			}
			
			pauseComboBox.addValueChangeListener(event -> {
				if (pauseComboBox.getValue() != null) {
					this.time.setPause((Integer) pauseComboBox.getValue());
				} else {
					this.time.setPause(null);
				}
				try {
					timeChanged();
				} catch (DateFormatException e) {
					Notification.show("Invalid time: " + e.getMessage(), Type.ERROR_MESSAGE);
				}
			});
			
			pauseComboBox.setReadOnly(readOnly);
		}
		return pauseComboBox;
	}

	public TextField getText() {
		if (this.textTextField == null) {
			textTextField = new TextField();
			textTextField.setWidth("400px");
			textTextField.setImmediate(true);
			
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
				personLabel.setValue(person.getName());
			}
		}
		return personLabel;
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

	private void setErrorMessage(AbstractComponent component, String errorMessage) {
		component.setDescription(errorMessage);
		component.addStyleName("error");
	}

	private String getHtml(List<String> messages) {
		if (messages != null && messages.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String message : messages) {
				if (sb.length() > 0) {
					sb.append("<br/>");
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
}
