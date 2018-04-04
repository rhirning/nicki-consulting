package org.mgnl.nicki.consulting.data;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.Clock;
import org.mgnl.nicki.consulting.core.helper.DateFormatException;
import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Time;
import org.mgnl.nicki.consulting.views.BaseView.READONLY;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;

public class TimeWrapper {

	private Time time;
	private int uniqueHash;
	private List<Member> members;
	
	private ComboBox memberComboBox;
	private DateField dayDateField;
	private TextField startTextField;
	private TextField endTextField;
	private ComboBox pauseComboBox;
	private TextField textTextField;
	private boolean readOnly;
	
	public TimeWrapper(Time time, List<Member> members, READONLY readonly) {
		super();
		this.time = time;
		uniqueHash = time.getUniqueHash();
		this.members = members;
		this.readOnly = (readonly == READONLY.TRUE);
	}
	
	public ComboBox getMember() {
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
		return memberComboBox;
	}
	
	public DateField getDay() {
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
		return dayDateField;
	}

	public TextField getStart() {
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
		return endTextField;
	}

	public ComboBox getPause() {
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
		return pauseComboBox;
	}

	public TextField getText() {
		textTextField = new TextField();
		textTextField.setWidth("200px");
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
		return textTextField;
	}

	public Time getTime() {
		return time;
	}
	
	public boolean isModified() {
		return time.getUniqueHash() != uniqueHash;
	}

	public void setMessages(List<String> messages) {
		String html = getHtml(messages);
		this.memberComboBox.setDescription(html);
		this.dayDateField.setDescription(html);
		this.startTextField.setDescription(html);
		this.endTextField.setDescription(html);
		this.textTextField.setDescription(html);
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
}
