package org.mgnl.nicki.consulting.views;

import java.util.List;

import org.mgnl.nicki.consulting.core.helper.PersonHelper;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.menu.application.View;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class PersonSelector extends VerticalLayout implements View {
	
	private Grid<Person> personsTable;

	private static final long serialVersionUID = 1751419839762448157L;
	private boolean isInit;
	private PersonSelectorParent personSelectorParent;

	public PersonSelector(PersonSelectorParent personSelectorParent) {
		this.personSelectorParent = personSelectorParent;
	}

	@Override
	public void init() {
		if (!isInit) {
			buildMainLayout();
			setMargin(true);
			setWidth("100%");
			personsTable.addColumn(Person::getName).setHeader("Name");
			personsTable.setSelectionMode(SelectionMode.SINGLE);
			personsTable.addSelectionListener(event -> {
				if (event.getFirstSelectedItem().isPresent()) {
					personSelectorParent.setPerson(event.getFirstSelectedItem().get());
				}
			});
			LoadPersons();
			isInit = true;
		}
	}

	private void LoadPersons() {
		
		List<Person> persons = PersonHelper.getPersons();
		personsTable.setItems(persons);
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setApplication(NickiApplication arg0) {
		// TODO Auto-generated method stub
		
	}

	
	private void buildMainLayout() {		
		// top-level component properties
		setWidth("100.0%");
		
		// personsTable
		personsTable = new Grid<Person>();
		personsTable.setWidth("-1px");
		personsTable.setHeight("-1px");
		add(personsTable);
	}

}
