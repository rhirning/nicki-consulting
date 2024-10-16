package org.mgnl.nicki.consulting.survey.notify;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;

import org.mgnl.nicki.scheduler.NickiSchedulerContextListener;

@WebListener
public class MyNickiSchedulerContextListener extends NickiSchedulerContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext(); 
        servletContext.setInitParameter("jobConfig", "/META-INF/nicki/jobs.json");
        super.contextInitialized(event);
    }
}

