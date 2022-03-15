package org.mgnl.nicki.consulting.survey.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyVote;
import org.mgnl.nicki.core.util.Classes;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.context.PrimaryKey;
import org.mgnl.nicki.db.profile.InitProfileException;

import com.vaadin.flow.component.textfield.TextField;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SurveyHelper {
	public static final String DB_CONTEXT_NAME = "projects";

	public static void addTopic(SurveyConfig survey, Person owner, String title, String description) {
		SurveyTopic surveyTopic = new SurveyTopic();
		surveyTopic.setSurveyId(survey.getId());
		surveyTopic.setName(title);
		surveyTopic.setDescription(description);
		surveyTopic.setOwner(owner.getUserId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			dbContext.create(surveyTopic);
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not save topic", e);
		}
	}
	
	public static List<SurveyConfig> getAllSurveys() {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return loadAll(dbContext, SurveyConfig.class);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException | ClassNotFoundException e) {
			log.error("Could not load surveys", e);
		}
		return new ArrayList<>();
	}	


	public static <T> List<T> loadAll(DBContext dbContext, Class<T> clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		T bean = Classes.newInstance(clazz.getName());
		return dbContext.loadObjects(bean, false);
	}
	
	public static <T> PrimaryKey create(T bean) throws SQLException, InitProfileException, NotSupportedException  {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return dbContext.create(bean);
		} catch (SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		return null;
	}
	
	public static <T> T update(T bean, String ...attributes) throws SQLException, InitProfileException, NotSupportedException  {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return dbContext.update(bean, attributes);
		} catch (SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		return null;
	}	
	
	public static List<SurveyTopic> getTopics(SurveyConfig survey) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return getTopics(dbContext, survey);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}

		return new ArrayList<>();
	}
	
	
	public static List<SurveyTopic> getTopics(DBContext dbContext, SurveyConfig survey) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		SurveyTopic surveyTopic = new SurveyTopic();
		surveyTopic.setSurveyId(survey.getId());
		List<SurveyTopic> surveyTopics = dbContext.loadObjects(surveyTopic, false);
		if (surveyTopics != null) {
			return surveyTopics;
		} else {
			return new ArrayList<>();
		}
	}
	
	
	public static List<SurveyTopicWrapper> getTopicWrappers(SurveyConfig survey, Person person) {
		List<SurveyTopicWrapper> surveyTopicWrappers = new ArrayList<>();
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			List<SurveyTopic> surveyTopics = getTopics(dbContext, survey);
			if (surveyTopics != null) {
				for (SurveyTopic topic : surveyTopics) {
					List<SurveyVote> surveyVotes = getSurveyVotes(dbContext, topic, person);
					SurveyTopicWrapper surveyTopicWrapper = new SurveyTopicWrapper();
					surveyTopicWrapper.setTopic(topic);
					surveyTopicWrapper.setVotes(surveyVotes);
					surveyTopicWrappers.add(surveyTopicWrapper);
				}
			}
			return surveyTopicWrappers;

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}

		return new ArrayList<>();
	}

	private static List<SurveyVote> getSurveyVotes(DBContext dbContext, SurveyTopic topic, Person person) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		try {
			return dbContext.loadObjects(surveyVote, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load survey votes", e);
		}

		return new ArrayList<>();
	}
}
