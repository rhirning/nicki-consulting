package org.mgnl.nicki.consulting.views;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.PasswordHelper;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.core.objects.DynamicObjectException;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.verify.PasswordRule;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;

public class PasswordView extends BaseView implements View {
	private static final long serialVersionUID = 7189958396828969386L;
	private Span title;
	private PasswordField oldPassword;
	private PasswordField newPassword;
	private PasswordField newPassword2;
	private Button saveButton;


	public PasswordView() {
		buildMainLayout();
		
		saveButton.addClickListener(event -> save());
	}

	private void save() {
		if (StringUtils.isBlank(oldPassword.getValue())) {
			Notification.show("Altes Password eingeben");
			return;
		}
		if (!PasswordHelper.verifyPassword(getLdapPerson(), oldPassword.getValue())) {
			Notification.show("Altes Password falsch");
			return;
		}
		if (!StringUtils.equals(newPassword.getValue(), newPassword2.getValue())) {
			Notification.show("Wiederholtes Passwort weicht ab");
			return;
		}
		
		if (!new PasswordRule("complexity=3!minlength=8").evaluate(newPassword.getValue(), null)) {
			Notification.show("Passwort ist zu einfach");
			return;
		}
		
		try {
			PasswordHelper.setPassword(getLdapPerson(), newPassword.getValue());
			Notification.show("Passwort wurde geändert");

			getApplication().logout();
		} catch (DynamicObjectException e) {
			Notification.show("Passwort konnte nicht gesetzt werden");
			return;
		}
	}
	
	private LdapPerson getLdapPerson() {
		LdapPerson ldapPerson = (LdapPerson) getApplication().getDoubleContext().getLoginContext().getUser();
		return ldapPerson;
	}

	@Override
	public void init() {
	}
	
	private void buildMainLayout() {
		setMargin(true);
		setSpacing(true);
		setWidth("100%");
		setHeight("100%");
		
		title = new Span("Passwortwechsel");
		oldPassword = new PasswordField("altes Passwort");
		newPassword = new PasswordField("neues Passwort");
		newPassword2 = new PasswordField("Neues Passwort wiederholen");
		saveButton = new Button("Passwort wechseln");

		add(title, oldPassword, newPassword, newPassword2, saveButton);
	}



}
