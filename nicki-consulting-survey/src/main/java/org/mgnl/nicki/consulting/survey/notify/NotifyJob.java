package org.mgnl.nicki.consulting.survey.notify;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.mgnl.nicki.consulting.core.helper.PersonHelper;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.helper.SurveyHelper;
import org.mgnl.nicki.consulting.survey.model.SurveyNotify;
import org.mgnl.nicki.core.config.Config;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.scheduler.Job;
import org.mgnl.nicki.scheduler.JobConfig;

@Slf4j
@Data
public class NotifyJob implements Job {
	private JobConfig jobConfig;
    public void run() {
    	String jobKey = jobConfig.getName();
    	if (!Config.getBoolean("nicki.survey.notify.enable", true)) {
    		log.info(getClass().getSimpleName() + ": Notification disabled");
    		return;
    	}
    	
    	List<SurveyNotify> openNotifies = SurveyHelper.getOpenNotifies();
    	if (openNotifies.size() > 0) {
	    	Map<String, Set<SurveyNotify>> notifyMap = new HashMap<>();
	    	for (SurveyNotify notify : openNotifies) {
	    		if (!notifyMap.containsKey(notify.getUserId())) {
	    			notifyMap.put(notify.getUserId(), new HashSet<>());
	    		}
	    		notifyMap.get(notify.getUserId()).add(notify);
	    	}
	    	
	    	for (String userId : notifyMap.keySet()) {
	    		Optional<Person> personOptional = PersonHelper.getPerson(userId);
	    		if (personOptional.isPresent()) {
	    			Person person = personOptional.get();
	    			String mail = person.getEmail();
	    			if (StringUtils.isNotBlank(mail)) {
	    				try {
							sendNotify(person, notifyMap.get(userId));
							log.info(getClass().getSimpleName() + " says: " + jobKey + " executing at " + new Date());
							for (SurveyNotify notify : notifyMap.get(userId)) {
								SurveyHelper.clearNotify(notify);
							}
						} catch (NotifyException e) {
							log.error("Error sending notify", e);
						}
	    			}
	    		}
	    	}
	
	        // This job simply prints out its job name and the
	        // date and time that it is running
    	} else {
    		log.info(getClass().getSimpleName() + " nothing to do");
    	}
    }

	private void sendNotify(Person person, Set<SurveyNotify> notifies) throws NotifyException {
		Map<String, Object> dataModel = new HashMap<>();
		dataModel.put("person", person);
		dataModel.put("notifies", notifies);
		dataModel.put("helper", new SurveyHelper());
		dataModel.put("dataHelper", new DataHelper());
		Mailer.send(person.getEmail(), dataModel);
		
	}

}
