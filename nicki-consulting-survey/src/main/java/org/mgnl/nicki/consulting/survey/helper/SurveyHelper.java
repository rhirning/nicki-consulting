package org.mgnl.nicki.consulting.survey.helper;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.objects.LdapPerson;
import org.mgnl.nicki.consulting.survey.model.SurveyChoice;
import org.mgnl.nicki.consulting.survey.model.SurveyConfig;
import org.mgnl.nicki.consulting.survey.model.SurveyConfigWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyNotify;
import org.mgnl.nicki.consulting.survey.model.SurveyTopic;
import org.mgnl.nicki.consulting.survey.model.SurveyTopicWrapper;
import org.mgnl.nicki.consulting.survey.model.SurveyVote;
import org.mgnl.nicki.consulting.views.NoApplicationContextException;
import org.mgnl.nicki.consulting.views.NoValidPersonException;
import org.mgnl.nicki.core.data.Period;
import org.mgnl.nicki.core.helper.DataHelper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
import org.mgnl.nicki.db.context.PrimaryKey;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.command.Command;
import org.mgnl.nicki.vaadin.base.command.CommandException;
import org.mgnl.nicki.vaadin.base.components.ConfirmDialog;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SurveyHelper {
	public static final String DB_CONTEXT_NAME = "projects";

	public static void addTopic(SurveyConfig survey, Person owner, String title, String description) {
		addTopic(survey, owner.getUserId(), title, description);
	}

	public static void addTopic(SurveyConfig survey, String owner, String title, String description) {
		SurveyTopic surveyTopic = new SurveyTopic();
		surveyTopic.setSurveyId(survey.getId());
		surveyTopic.setName(title);
		surveyTopic.setDescription(description);
		surveyTopic.setOwner(owner);
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			dbContext.create(surveyTopic);
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not save topic", e);
		}
	}
	
	public static List<SurveyConfig> getAllActiveSurveys() {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyConfig surveyConfig = new SurveyConfig();
			String filter = getActiveFilter(dbContext);
			String orderbBy = null;
			return dbContext.loadObjects(surveyConfig, false, filter, orderbBy);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		return new ArrayList<>();
	}
	
	public static SurveyConfig getSurvey(long id) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyConfig surveyConfig = new SurveyConfig();
			surveyConfig.setId(id);
			return dbContext.loadObject(surveyConfig, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		return null;
	}
	
	private static String getActiveFilter(DBContext dbContext) {
		Calendar today = Period.getTodayCalendar();
		StringBuilder sb = new StringBuilder();
		sb.append("(START_TIME IS NULL OR START_TIME <= ").append(dbContext.toTimestamp(today.getTime())).append(")");
		sb.append(" AND (VISIBLE_TIME IS NULL OR VISIBLE_TIME >= ").append(dbContext.toTimestamp(today.getTime())).append(")");
		return sb.toString();
	}

	public static List<SurveyConfig> getAllSurveys() {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyConfig surveyConfig = new SurveyConfig();
			return dbContext.loadObjects(surveyConfig, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		return new ArrayList<>();
	}
	
	public static <T> PrimaryKey create(T bean) throws SQLException, InitProfileException, NotSupportedException  {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return dbContext.create(bean);
		} catch (SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}
		return null;
	}
	
	public static <T> void delete(T bean) throws SQLException, InitProfileException, NotSupportedException  {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			dbContext.delete(bean);
		} catch (SQLException | InitProfileException e) {
			log.error("Could not delete surveys", e);
		}
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
			log.error("Could not load topics", e);
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

	public static void updateNotifies(SurveyConfig survey, Person person) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			for (SurveyNotify notify : getNotifies(dbContext, survey)) {
				if (!StringUtils.equals(person.getUserId(), notify.getUserId())) {
					notify.setModified(new Date());
					dbContext.update(notify, "modified");
				}
			}
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not update notifies", e);
		}
	}
	
	public static List<SurveyNotify> getNotifies(SurveyConfig survey) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return getNotifies(dbContext, survey);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load topics", e);
		}

		return new ArrayList<>();
	}

	public static void clearNotify(SurveyNotify notify) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			notify.setModified(null);
			dbContext.update(notify, "modified");
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not load topics", e);
		}
	}
	
	public static List<SurveyNotify> getOpenNotifies() {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return getOpenNotifies(dbContext);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load topics", e);
		}

		return new ArrayList<>();
	}
	
	
	public static List<SurveyNotify> getOpenNotifies(DBContext dbContext) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		SurveyNotify surveyNotify = new SurveyNotify();
		List<SurveyNotify> surveyNotifies = dbContext.loadObjects(surveyNotify, false, "NOT MODIFIED IS NULL", null);
		if (surveyNotifies != null) {
			return surveyNotifies;
		} else {
			return new ArrayList<>();
		}
	}
	
	
	public static List<SurveyNotify> getNotifies(DBContext dbContext, SurveyConfig survey) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		SurveyNotify surveyNotify = new SurveyNotify();
		surveyNotify.setSurveyId(survey.getId());
		List<SurveyNotify> surveyNotifies = dbContext.loadObjects(surveyNotify, false);
		if (surveyNotifies != null) {
			return surveyNotifies;
		} else {
			return new ArrayList<>();
		}
	}
	
	public static List<SurveyChoice> getChoices(SurveyConfig survey) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return getChoices(dbContext, survey);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load choices", e);
		}

		return new ArrayList<>();
	}

	
	public static List<SurveyChoice> getChoices(DBContext dbContext, SurveyConfig survey) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		return getChoices(dbContext, survey.getId());
	}
	
	public static List<SurveyChoice> getChoices(DBContext dbContext, long surveyId) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		SurveyChoice surveyChoice = new SurveyChoice();
		surveyChoice.setSurveyId(surveyId);
		List<SurveyChoice> surveyChoices = dbContext.loadObjects(surveyChoice, false);
		if (surveyChoices != null) {
			return surveyChoices.stream().sorted((c1,c2) -> c1.getWeight().compareTo(c2.getWeight())).collect(Collectors.toList());
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
					SurveyTopicWrapper surveyTopicWrapper = new SurveyTopicWrapper(survey, topic, person);
					surveyTopicWrappers.add(surveyTopicWrapper);
				}
			}
			return surveyTopicWrappers;

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load surveys", e);
		}

		return new ArrayList<>();
	}

	public static List<SurveyVote> getSurveyVotes(DBContext dbContext, SurveyTopic topic, Person person) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		try {
			return dbContext.loadObjects(surveyVote, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load survey votes", e);
		}

		return new ArrayList<>();
	}

	private static List<SurveyVote> getSurveyVotes(SurveyConfig surveyConfig) {

		List<SurveyVote> votes = new ArrayList<>();
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			for (SurveyTopic topic : getTopics(surveyConfig)) {
				SurveyVote surveyVote = new SurveyVote();
				surveyVote.setSurveyTopicId(topic.getId());
				try {
					votes.addAll(dbContext.loadObjects(surveyVote, false));
				} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
					log.error("Could not load survey votes", e);
				}
			}
		} catch (SQLException e) {
			log.error("Could not load surveyVotes", e);
		}
		return votes;
	}
	

	
	public static Person getActivePerson(NickiApplication application) throws NoValidPersonException, NoApplicationContextException {

		if (application == null) {
			throw new NoApplicationContextException();
		}
		LdapPerson ldapPerson = (LdapPerson) application.getDoubleContext().getLoginContext().getUser();
		Person person = new Person();
		person.setUserId(ldapPerson.getName());

		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.loadObject(person, false);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load person", e);
			throw new NoValidPersonException(ldapPerson.getDisplayName());
		}
	}

	public static boolean isDeleteAllowed(SurveyConfig surveyConfig) {
		if (getSurveyVotes(surveyConfig).size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static Optional<SurveyVote> getSurveyVote(SurveyTopic topic, Person person) {

		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyVote surveyVote = new SurveyVote();
			surveyVote.setSurveyTopicId(topic.getId());
			surveyVote.setUserId(person.getUserId());
			try {
				return Optional.ofNullable(dbContext.loadObject(surveyVote, false));
			} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
				log.error("Could not load survey votes", e);
			}
		} catch (SQLException e) {
			log.error("Could not load surveyVotes", e);
		}
		return Optional.empty();
	}

	public static List<SurveyVote> getSurveyVotes(SurveyTopic topic) {

		List<SurveyVote> votes = new ArrayList<>();
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyVote surveyVote = new SurveyVote();
			surveyVote.setSurveyTopicId(topic.getId());
			try {
				votes.addAll(dbContext.loadObjects(surveyVote, false));
			} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
				log.error("Could not load survey votes", e);
			}
		} catch (SQLException e) {
			log.error("Could not load surveyVotes", e);
		}
		return votes;
	}

	public static void setVote(SurveyTopic topic, Person person, Long choiceId, String comment) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		surveyVote.setUserId(person.getUserId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			if (dbContext.exists(surveyVote)) {
				surveyVote.setSurveyChoiceId(choiceId);
				surveyVote.setComment(comment);
				dbContext.update(surveyVote, "surveyChoiceId", "comment");
			} else {
				surveyVote.setSurveyChoiceId(choiceId);
				surveyVote.setComment(comment);
				dbContext.create(surveyVote);
			}
	
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not update surveyVote: " + surveyVote, e);
		}
	}

	public static void updateVote(SurveyTopic topic, Person person, Long choiceId) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		surveyVote.setUserId(person.getUserId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			if (dbContext.exists(surveyVote)) {
				surveyVote = dbContext.loadObject(surveyVote, false);
				surveyVote.setSurveyChoiceId(choiceId);
				dbContext.update(surveyVote, "surveyChoiceId");
			} else {
				surveyVote.setSurveyChoiceId(choiceId);
				dbContext.create(surveyVote);
			}
	
		} catch (SQLException | InitProfileException | NotSupportedException | InstantiationException | IllegalAccessException e) {
			log.error("Could not update surveyVote: " + surveyVote, e);
		}
	}

	public static void updateVoteComment(SurveyTopic topic, Person person, String comment) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		surveyVote.setUserId(person.getUserId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			if (dbContext.exists(surveyVote)) {
				surveyVote = dbContext.loadObject(surveyVote, false);
				surveyVote.setComment(comment);
				dbContext.update(surveyVote, "comment");
			} else {
				surveyVote.setComment(comment);
				dbContext.create(surveyVote);
			}
	
		} catch (SQLException | InitProfileException | NotSupportedException | InstantiationException | IllegalAccessException e) {
			log.error("Could not update surveyVote: " + surveyVote, e);
		}
	}

	public static void removeVote(SurveyTopic topic, Person person) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		surveyVote.setUserId(person.getUserId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			if (dbContext.exists(surveyVote)) {
				surveyVote = dbContext.loadObject(surveyVote, false);
				if (StringUtils.isBlank(surveyVote.getComment())) {
					dbContext.delete(surveyVote);
				} else {
					surveyVote.setSurveyChoiceId(null);
					dbContext.update(surveyVote, "surveyChoiceId");
				}
			}	
		} catch (SQLException | InitProfileException | InstantiationException | IllegalAccessException | NotSupportedException e) {
			log.error("Could not remove surveyVote: " + surveyVote, e);
		}
	}

	public static boolean getNotify(SurveyConfigWrapper survey, Person person) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyNotify surveyNotify = new SurveyNotify();
			surveyNotify.setSurveyId(survey.getId());
			surveyNotify.setUserId(person.getUserId());
			return dbContext.exists(surveyNotify);
		} catch (SQLException | InitProfileException e) {
			log.error("Could not load surveyVotes", e);
		}
		return false;
	}

	public static void addNotify(SurveyConfigWrapper survey, Person person) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyNotify surveyNotify = new SurveyNotify();
			surveyNotify.setSurveyId(survey.getId());
			surveyNotify.setUserId(person.getUserId());
			if (!dbContext.exists(surveyNotify)) {
				dbContext.create(surveyNotify);
			}
		} catch (SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not load surveyVotes", e);
		}
	}

	public static void removeNotify(SurveyConfigWrapper survey, Person person) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			SurveyNotify surveyNotify = new SurveyNotify();
			surveyNotify.setSurveyId(survey.getId());
			surveyNotify.setUserId(person.getUserId());
			if (dbContext.exists(surveyNotify)) {
				try {
					surveyNotify = dbContext.loadObject(surveyNotify, false);
					dbContext.delete(surveyNotify);
				} catch (NotSupportedException e) {
					Notification.show("Löschen wird nicht unterstützt", Type.HUMANIZED_MESSAGE);
				}
			}
		} catch (SQLException | InitProfileException | InstantiationException | IllegalAccessException e) {
			log.error("Could not load surveyVotes", e);
		}
	}

	public static void deleteTopics(SurveyConfig survey) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			deleteTopics(dbContext, survey);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException | NotSupportedException e) {
			log.error("Could not load topics", e);
		}
	}

	public static void deleteTopics(DBContext dbContext, SurveyConfig survey) throws SQLException, InitProfileException, InstantiationException, IllegalAccessException, NotSupportedException {
		for (SurveyTopic topic : getTopics(dbContext, survey)) {
			deleteTopic(dbContext, topic);
		}
	}

	public static void deleteChoices(SurveyConfig survey) throws NotSupportedException {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			deleteChoices(dbContext, survey);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load topics", e);
		}
	}

	public static void deleteChoice(SurveyChoice choice) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			deleteChoice(dbContext, choice);
		} catch (SQLException | NotSupportedException | InitProfileException | InstantiationException | IllegalAccessException e) {
			log.error("Could not delete choice", e);
		}
	}

	public static void deleteChoice(DBContext dbContext, SurveyChoice choice) throws SQLException, InitProfileException, NotSupportedException, InstantiationException, IllegalAccessException {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyChoiceId(choice.getId());
		List<SurveyVote> votes = dbContext.loadObjects(surveyVote, false);
		if (votes != null) {
			for (SurveyVote vote : votes) {
				vote.setSurveyChoiceId(null);
				dbContext.update(vote, "surveyChoiceId");
			}
		}
		dbContext.delete(choice);
	}

	public static void deleteTopic(DBContext dbContext, SurveyTopic topic) throws SQLException, InitProfileException, NotSupportedException, InstantiationException, IllegalAccessException {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		List<SurveyVote> votes = dbContext.loadObjects(surveyVote, false);
		if (votes != null) {
			for (SurveyVote vote : votes) {
				vote.setSurveyChoiceId(null);
				dbContext.update(vote, "surveyChoiceId");
			}
		}
		dbContext.delete(topic);
	}

	public static void deleteChoices(DBContext dbContext, SurveyConfig survey) throws SQLException, InitProfileException, InstantiationException, IllegalAccessException, NotSupportedException {
		for (SurveyChoice choice : getChoices(dbContext, survey)) {
			dbContext.delete(choice);
		}
	}

	public static void deleteNotifies(SurveyConfig survey) throws NotSupportedException {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			deleteNotifies(dbContext, survey);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load topics", e);
		}
	}

	public static void deleteNotifies(DBContext dbContext, SurveyConfig survey) throws SQLException, InitProfileException, InstantiationException, IllegalAccessException, NotSupportedException {
		for (SurveyNotify surveyNotify : getNotifies(dbContext, survey)) {
			dbContext.delete(surveyNotify);
		}
	}

	public static void deleteSurvey(SurveyConfig survey) {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			deleteTopics(dbContext, survey);
			deleteNotifies(dbContext, survey);
			deleteChoices(dbContext, survey);
			
		} catch ( InitProfileException | InstantiationException | IllegalAccessException | SQLException | NotSupportedException e) {
			log.error("Could not load topics", e);
		}
	}

	public static void cloneSurvey(SurveyConfig survey, Person person) {
		SurveyConfig copy = new SurveyConfig();
		copy.setAddComment(survey.getAddComment());
		copy.setAddTopic(survey.getAddTopic());
		copy.setDescription(survey.getDescription());
		copy.setName("Kopie von " + survey.getName());
		copy.setOwner(person.getUserId());
			copy.setStart(survey.getStart());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			PrimaryKey primaryKey = dbContext.create(copy);
			long surveyId = primaryKey.getLong("ID");
			for (SurveyTopic topic : getTopics(dbContext, survey)) {
				topic.setId(null);
				topic.setSurveyId(surveyId);
				topic.setOwner(person.getUserId());
				dbContext.create(topic);
			}
			for (SurveyChoice choice : getChoices(dbContext, survey)) {
				choice.setId(null);
				choice.setSurveyId(surveyId);
				dbContext.create(choice);
			}
			
		} catch ( InitProfileException | InstantiationException | IllegalAccessException | SQLException | NotSupportedException e) {
			log.error("Could not copy survey", e);
		}
	}

	public static void deleteVotes(SurveyConfig survey) throws NotSupportedException {
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			for (SurveyChoice choice : getChoices(dbContext, survey.getId())) {
				deleteVotes(dbContext, choice);
			}
			for (SurveyTopic topic: getTopics(dbContext, survey)) {
				deleteVotes(dbContext, topic);
			}
			
		} catch ( InitProfileException | InstantiationException | IllegalAccessException | SQLException e) {
			log.error("Could not load topics", e);
		}
	}

	public static void deleteVotes(DBContext dbContext, SurveyChoice choice) throws NotSupportedException {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyChoiceId(choice.getId());
		try {
			List<SurveyVote> surveyVotes = dbContext.loadObjects(surveyVote, false);
			if (surveyVotes != null) {
				for (SurveyVote vote : surveyVotes) {
					dbContext.delete(vote);
				}
			}
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not delete votes topics", e);
		}
	}

	public static void deleteVotes(DBContext dbContext, SurveyTopic topic) throws NotSupportedException {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		try {
			List<SurveyVote> surveyVotes = dbContext.loadObjects(surveyVote, false);
			if (surveyVotes != null) {
				for (SurveyVote vote : surveyVotes) {
					dbContext.delete(vote);
				}
			}
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not delete votes topics", e);
		}
	}

	public static long countVotes(SurveyTopic topic, SurveyChoice surveyChoice) {
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		surveyVote.setSurveyChoiceId(surveyChoice.getId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			return dbContext.count(surveyVote, null);
		} catch (SQLException | InitProfileException e) {
			log.error("Could not update surveyVote: " + surveyVote, e);
		}
		return 0;
	}

	public static int getValue(SurveyTopic topic) {
		int value = 0;
		SurveyVote surveyVote = new SurveyVote();
		surveyVote.setSurveyTopicId(topic.getId());
		try (DBContext dbContext = DBContextManager.getContext(DB_CONTEXT_NAME)) {
			List<SurveyChoice> choices = getChoices(dbContext, topic.getSurveyId());
			Map<Long, Integer> choicesMap = new HashMap<>();
			if (choices != null) {
				choices.stream().forEach(c -> {
					choicesMap.put(c.getId(), c.getWeight() != null ? c.getWeight() : 0);
				});
			}
			List<SurveyVote> votes = dbContext.loadObjects(surveyVote, false);
			if (votes != null) {
				for (SurveyVote vote : votes) {
					if (vote.getSurveyChoiceId() != null) {
						if (choicesMap.containsKey(vote.getSurveyChoiceId())) {
							value += choicesMap.get(vote.getSurveyChoiceId());
						}
					}
				}
			}
		} catch (SQLException | InitProfileException | InstantiationException | IllegalAccessException e) {
			log.error("Could not update surveyVote: " + surveyVote, e);
		}
		return value;
	}

	public static List<String> getComments(SurveyTopic surveyTopic) {
		List<SurveyVote> votes = getSurveyVotes(surveyTopic);
		List<String> comments = new ArrayList<>();
		for (SurveyVote vote : votes) {
			if (StringUtils.isNotBlank(vote.getComment())) {
				comments.add(vote.getComment());
			}
		}
		return comments;
	}

	public static boolean isExist(Object bean) throws SQLException, InitProfileException {
		try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
			return dbContext.exists(bean);
		}
	}
	
	public static <T> void confirm(String title, T bean, Consumer<T> action) {
		showConfirmDialog(title, bean, action);
	}

	
	private static <T> void showConfirmDialog(String windowTitle, T bean, Consumer<T> action) {
		ConfirmDialog editWindow = new ConfirmDialog();
		editWindow.setCommand(getCommand(windowTitle, bean, b -> {
			action.accept(b);
			editWindow.close();
		}));

		editWindow.setModal(true);
		editWindow.setWidth("600px");
		editWindow.setHeight("600px");
		editWindow.open();
	}

	private static <T> Command getCommand(String windowTitle, T bean, Consumer<T> action) {
		return new Command() {
			
			@Override
			public String getTitle() {
				return windowTitle;
			}
			
			@Override
			public String getHeadline() {
				return "Sind Sie sicher";
			}
			
			@Override
			public String getErrorText() {
				return "";
			}
			
			@Override
			public String getConfirmCaption() {
				return "Ja";
			}
			
			@Override
			public String getCancelCaption() {
				return "Abbrechen";
			}
			
			@Override
			public void execute() throws CommandException {
				action.accept(bean);				
			}
		};
	}
}
