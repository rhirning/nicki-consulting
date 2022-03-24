package org.mgnl.nicki.consulting.survey.notify;

import org.mgnl.nicki.template.engine.TemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.core.auth.InvalidPrincipalException;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.template.engine.ConfigurationFactory.TYPE;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class Mailer {
	private static final String FROM				= "nicki.survey.notify.mail.from";
	private static final String MAIL_USER			= "nicki.survey.notify.mail.server.username";
	private static final String MAIL_PASSWORD		= "nicki.survey.notify.mail.server.password";
	private static final String MAIL_HOST			= "nicki.survey.notify.mail.server.host";
	private static final String MAIL_PORT			= "nicki.survey.notify.mail.server.port";
	private static final String TEMPLATE			= "nicki.survey.notify.mail.template";
	private static final String CHARSET				= "UTF-8";


	public static void send(String mail, Map<String, Object> dataModel) throws NotifyException {
		TemplateEngine engine = TemplateEngine.getInstance(TYPE.CLASSPATH);
		String template = Config.getString(TEMPLATE);
		
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		ByteArrayOutputStream subject = new ByteArrayOutputStream();
		try {
			IOUtils.copy(engine.executeTemplate(template + ".body.ftl", dataModel, "UTF-8"), body);
			IOUtils.copy(engine.executeTemplate(template + ".subject.ftl", dataModel, "UTF-8"), subject);
		} catch (IOException | TemplateException | InvalidPrincipalException e) {
			throw new NotifyException("Error generating mail", e);
		}
		
		try {
			sendMail(mail, subject.toString(CHARSET), body.toString(CHARSET));
		} catch (Exception e) {
			throw new NotifyException("Error sending mail", e);
		}
	}
	

	public static void sendMail(String mail, String subject, String body) throws AddressException, MessagingException {
		String to = mail;

		String from = Config.getString(FROM, null);
		String username = Config.getString(MAIL_USER, null);
		String password = Config.getString(MAIL_PASSWORD, null);
		String host = Config.getString(MAIL_HOST, null);
		String port = Config.getString(MAIL_PORT, null);

		Properties props = new Properties();
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		if (StringUtils.isNotBlank(port)) {
			props.put("mail.smtp.port", port);
		}
		Session session;
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {
			session = Session.getInstance(props,null);
		}


			// Create a default MimeMessage object.
			Message message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			// Set CC: header field of the header.
//			message.setRecipients(Message.RecipientType.CC,
//					InternetAddress.parse(cc));
			// Set BCC: header field of the header.
//			message.setRecipients(Message.RecipientType.BCC,
//					InternetAddress.parse("idm@provinzial.de"));
			// Set Subject: header field
			message.setSubject(subject);

			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Now set the actual message
			messageBodyPart.setContent(body, "text/html");

			// Create a multipar message
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Send the complete message parts
			message.setContent(multipart, "text/html");

			// Send message
			Transport.send(message);

			log.info("Sent message successfully to " + to);


	}

}
