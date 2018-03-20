package org.mgnl.nicki.consulting.app;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.mgnl.nicki.core.context.Target;
import org.mgnl.nicki.core.context.TargetFactory;
import org.mgnl.nicki.dynamic.objects.objects.Person;
import org.mgnl.nicki.vaadin.base.application.AccessGroup;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;

@AccessGroup(name = {"nickiAdmins", "IDM-Development"})
@SuppressWarnings("serial")
public class Projects extends NickiApplication implements Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(Projects.class);
	private MainView mainView;


	@Override
	public Component getEditor() {
		
		mainView = new MainView(null);
		mainView = new MainView((Person) getNickiContext().getUser());
		
		try {
			mainView.addNavigation(this, "/META-INF/applications/projects.json");
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException
				| ClassNotFoundException e) {
			LOG.error("Error in config", e);
		}

		

		mainView.initNavigation();
		
		return mainView;
	}

	@Override
	public Target getTarget() {
		return TargetFactory.getDefaultTarget();
	}

	@Override
	public String getI18nBase() {
		return "nicki.consulting.app";
	}

}
