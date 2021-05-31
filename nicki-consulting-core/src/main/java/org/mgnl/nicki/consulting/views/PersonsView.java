package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.List;

import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class PersonsView extends BaseView implements View {
	
	private HorizontalLayout horizontalLayout;
	
	private VerticalLayout personsLayout;
	
	private VerticalLayout personLayout;
		
	private Button newPersonButton;
	
	private Grid<Person> personsTable;


	private static final long serialVersionUID = 1751419839762448157L;
	private static final Logger LOG = LoggerFactory.getLogger(PersonsView.class);
	private boolean isInit;
	private Dialog newPersonWindow;

	public PersonsView() {
		buildMainLayout();
		
		newPersonButton.addClickListener(event -> {
			showNewPersonView();
		});
		personsTable.setSelectionMode(SelectionMode.SINGLE);
		personsTable.addSelectionListener(event -> {
			if (event.getFirstSelectedItem().isPresent()) {
				showPerson(event.getFirstSelectedItem().get());
			} else {
				hidePerson();
			}
		});
	}

	private void hidePerson() {
		personLayout.removeAll();
	}

	private void showPerson(Person person) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				hidePerson();
				LoadPersons();
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setDbBean(person);
		personLayout.removeAll();
		personLayout.add(beanViewer);
	}

	private void showNewPersonView() {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				newPersonWindow.close();
				hidePerson();
				LoadPersons();
			}
		});
		beanViewer.setDbContextName("projects");
		try {
			beanViewer.init(Person.class);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newPersonWindow = new DialogBase("Neue Person", beanViewer);
		newPersonWindow.setModal(true);
		newPersonWindow.open();
	}

	@Override
	public void init() {
		if (!isInit) {
			personsTable.addColumn(Person::getName).setHeader("Name");
			LoadPersons();
			isInit = true;
			showPersonsLayout(true);
		}
	}

	private void showPersonsLayout(boolean show) {
		personsLayout.setVisible(show);
		
	}

	private void LoadPersons() {
		Person person = new Person();
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			List<Person> persons = dbContext.loadObjects(person, false);
			personsTable.setItems(persons);

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load customers", e);
		}
		
	}

	
	private void buildMainLayout() {
		setWidth("100%");
		setHeight("100%");
		
		// horizontalLayout
		horizontalLayout = buildHorizontalLayout_1();
		add(horizontalLayout);
	}

	
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		horizontalLayout.setHeight("100.0%");
		horizontalLayout.setMargin(false);
		
		// personsLayout
		personsLayout = buildPersonsLayout();
		horizontalLayout.add(personsLayout);
				
		// personLayout
		personLayout = new VerticalLayout();
		personLayout.setWidth("100.0%");
		personLayout.setHeight("100.0%");
		personLayout.setMargin(false);
		horizontalLayout.add(personLayout);
		
		return horizontalLayout;
	}

	
	private VerticalLayout buildPersonsLayout() {
		// common part: create layout
		personsLayout = new VerticalLayout();
		personsLayout.setWidth("100%");
		personsLayout.setHeight("100%");
		personsLayout.setMargin(false);
		personsLayout.setSpacing(true);
		
		// newPersonButton
		newPersonButton = new Button();
		newPersonButton.setText("Neu");
		newPersonButton.setWidth("-1px");
		newPersonButton.setHeight("-1px");
		personsLayout.add(newPersonButton);
		
		// personsTable
		personsTable = new Grid<>();
		personsTable.setWidth("100%");
		personsTable.setHeight("100%");
		personsLayout.add(personsTable);
		personsLayout.setFlexGrow(1, personsTable);
		
		return personsLayout;
	}

}
