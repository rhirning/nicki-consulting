package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Person;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.data.MemberWrapper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CustomersView extends BaseView implements View {
	
	private HorizontalLayout contentLayout;
	
	private VerticalLayout memberLayout;
	
	private HorizontalLayout membersTableLayout;
	
	private VerticalLayout memberActionsLayout;
	
	private Button deleteMemberButton;
	
	private Button newMemberButton;
	
	private Button editMemberButton;
	
	private Grid<MemberWrapper> membersTable;
	
	private VerticalLayout projectLayout;
	
	private HorizontalLayout projectsTableLayout;
	
	private VerticalLayout projectActionsLayout;
	
	private Button deleteProjectButton;
	
	private Button newProjectButton;
	
	private Button editProjectButton;
	
	private Grid<Project> projectsTable;
	
	private VerticalLayout customerLayout;
	
	private HorizontalLayout customersTableLayout;
	
	private VerticalLayout customerActionsLayout;
	
	private Button deleteCustomerButton;
	
	private Button newCustomerButton;
	
	private Button editCustomerButton;
	
	private Grid<Customer> customersTable;
	
	private VerticalLayout tablesLayout;
	
	private VerticalLayout editLayout;

	private static final long serialVersionUID = 1751419839762448157L;
	private static final Logger LOG = LoggerFactory.getLogger(CustomersView.class);
	private boolean isInit;

	private DialogBase editWindow;
	private DialogBase newProjectWindow;
	private DialogBase newMemberWindow;
	private DialogBase selectPersonWindow;

	public CustomersView() {
		buildMainLayout();

		newCustomerButton.addClickListener(event ->	showEditCustomerView(Optional.empty()));
		editCustomerButton.addClickListener(event -> {
			if (!customersTable.asSingleSelect().isEmpty()) {
				showEditCustomerView(Optional.of(customersTable.asSingleSelect().getValue()));
			}
		});
		
		deleteCustomerButton.addClickListener(event -> deleteCustomer());
		
		customersTable.setSelectionMode(SelectionMode.SINGLE);
		customersTable.addSelectionListener(event -> {
			if (event.getFirstSelectedItem().isPresent()) {
				showProjects(event.getFirstSelectedItem().get());
			} else {
				showProjectsLayout(false);
			}
		});

		newProjectButton.addClickListener(event -> showEditProjectView(Optional.empty()));
		editProjectButton.addClickListener(event -> {
			if (!projectsTable.asSingleSelect().isEmpty()) {
				showEditProjectView(Optional.of(projectsTable.asSingleSelect().getValue()));
			}
					});
		deleteProjectButton.addClickListener(event -> deleteProject());
		
		projectsTable.setSelectionMode(SelectionMode.SINGLE);
		projectsTable.addSelectionListener(event -> {
			if (event.getFirstSelectedItem().isPresent()) {
				showMembers(event.getFirstSelectedItem().get());
			} else {
				showMembersLayout(false);
			}
		});

		newMemberButton.addClickListener(event -> showNewMemberView());
		editMemberButton.addClickListener(event -> {
			if (!membersTable.asSingleSelect().isEmpty()) {
				showEditMemberView(Optional.of(membersTable.asSingleSelect().getValue()));
			}
					});
		deleteMemberButton.addClickListener(event -> deleteMember());
		membersTable.setSelectionMode(SelectionMode.SINGLE);
	}

	private void deleteMember() {
		Optional<MemberWrapper> memberWrapperOptional = getSelectedItem(membersTable);
		if (!memberWrapperOptional.isPresent()) {
			Notification.show("Bitte erst Projektmitglied auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			try {
				if (TimeHelper.hasTimeEntries(memberWrapperOptional.get().getMember())) {
					Notification.show("Es gibt Buchungen für diese Projektmitglied", Type.HUMANIZED_MESSAGE);
				} else {
					TimeHelper.delete(memberWrapperOptional.get().getMember());
					showMembersLayout(false);
					showProjectsLayout(false);
					loadCustomers();
				}
			} catch (SQLException | InitProfileException e) {
				LOG.error("Error accessing db", e);
				Notification.show("Das Projektmitglied konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	private void deleteProject() {
		Optional<Project> projectOptional = getSelectedItem(projectsTable);
		if (!projectOptional.isPresent()) {
			Notification.show("Bitte erst Projekt auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			Project project = projectOptional.get();
			try {
				if (TimeHelper.hasMembers(project)) {
					Notification.show("Das Projekt hat noch Projektmitglieder", Type.HUMANIZED_MESSAGE);
				} else {
					TimeHelper.delete(project);
					showMembersLayout(false);
					showProjectsLayout(false);
					loadCustomers();

				}
			} catch (SQLException | InitProfileException e) {
				LOG.error("Error accessing db", e);
				Notification.show("Das Projekt konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	private void deleteCustomer() {
		Optional<Customer> customerOptional = getSelectedItem(customersTable);
		if (!customerOptional.isPresent()) {
			Notification.show("Bitte erst Kunde auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			Customer customer = customerOptional.get();
			try {
				if (TimeHelper.hasProjects(customer)) {
					Notification.show("Der Kunde besitzt noch Projekte", Type.HUMANIZED_MESSAGE);
				} else {
					TimeHelper.delete(customer);
					showMembersLayout(false);
					showProjectsLayout(false);
					loadCustomers();
				}
			} catch (SQLException | InitProfileException e) {
				LOG.error("Error accessing db", e);
				Notification.show("Der Kunde konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	private void showEditCustomer(Customer customer) {
		showProjectsLayout(false);
		showMembersLayout(false);
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				loadCustomers();
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setWidth("400px");
		beanViewer.setHeightFull();
		beanViewer.setDbBean(customer);
		customerLayout.removeAll();
		customerLayout.add(beanViewer);
		showProjects(customer);
	}

	private void showProjects(Customer customer) {
		if (customer != null) {
			showProjectsLayout(true);
			loadProjects(customer);
		} else {
			showProjectsLayout(false);
		}
	}

	private void showEditCustomerView(Optional<Customer> customer) {
		showEditView(Customer.class, customer, "Neuer Kunde", "Kunde bearbeiten");
	}

	private void showEditProjectView(Optional<Project> project) {
		showEditView(Project.class, project, "Neues Projekt", "Projekt bearbeiten");
	}

	private void showEditMemberView(Optional<MemberWrapper> memberWrapper) {
		if (memberWrapper.isPresent()) {
			showEditView(Member.class, Optional.of(memberWrapper.get().getMember()), "Neues Mitglied", "Mitglied bearbeiten");
		}
	}

	private <T> void showEditView(Class<T> clazz, Optional<T> object, String newCaption, String editCaption) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				editWindow.close();
				loadCustomers();
				showCustomersLayout(true);
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidth("400px");
		String windowTitle;
		if (!object.isPresent()) {
			try {
				beanViewer.init(clazz);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			windowTitle = newCaption;
		} else {
			beanViewer.setDbBean(object.get());
			windowTitle = editCaption;
		}
		editWindow = new DialogBase(windowTitle, beanViewer);
		editWindow.setModal(true);
		editWindow.setHeightFull();
		editWindow.open();
	}

	private void hideProject() {
		projectLayout.removeAll();
		showProjectsLayout(true);
		showMembersLayout(false);
	}

	private void showEditProject(Project project) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				hideProject();
				if (getSelectedItem(customersTable).isPresent())
				loadProjects(getSelectedItem(customersTable).get());
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setWidth("400px");
		beanViewer.setHeightFull();
		beanViewer.setDbBean(project);
		
		projectLayout.removeAll();
		projectLayout.add(beanViewer);
		showMembers(project);
	}

	private void showMembers(Project project) {
		showMembersLayout(true);
		loadMembers(project);
	}

	private void showNewProjectView() {
		Optional<Customer> customer = getSelectedItem(customersTable);
		if (customer.isPresent()) {
			DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
				
				@Override
				public void close(Component component) {
					newProjectWindow.close();
					hideProject();
					if (getSelectedItem(customersTable).isPresent()) {
						loadProjects(getSelectedItem(customersTable).get());
					}
				}
			});
			beanViewer.setDbContextName("projects");
			beanViewer.setWidth("400px");
			try {
				beanViewer.init(Project.class, customer.get());
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			newProjectWindow = new DialogBase("Neues Projekt", beanViewer);
			newProjectWindow.setModal(true);
			newProjectWindow.open();
		}
	}

	private void showNewMemberView() {
		PersonSelector selector = new PersonSelector(person -> {
			selectPersonWindow.close();
			setPerson(person);
			
		});
		selector.init();
		selectPersonWindow = new DialogBase("Person wählen", selector);
		selectPersonWindow.setModal(true);
		selectPersonWindow.open();
	}

	private void setPerson(Person person) {
		Optional<Project> project = getSelectedItem(projectsTable);
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				newMemberWindow.close();
				hideMember();
				if (getSelectedItem(projectsTable).isPresent()) {
					loadMembers(getSelectedItem(projectsTable).get());
				}
			}
		});
		beanViewer.setDbContextName("projects");
		try {
			beanViewer.init(Member.class, project.isPresent()?project.get():null, person);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newMemberWindow = new DialogBase("Neues Projektmitglied", beanViewer);
		newMemberWindow.setModal(true);
		newMemberWindow.open();
	}

	private void hideMember() {
		memberLayout.removeAll();
	}

	private void showEditMember(Optional<MemberWrapper> memberWrapper) {
		if (memberWrapper.isPresent()) {
			DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
				
				@Override
				public void close(Component component) {
					hideMember();
					if (getSelectedItem(projectsTable).isPresent()) {
						loadMembers(getSelectedItem(projectsTable).get());
					}
				}
			});
			beanViewer.setDbContextName("projects");
			beanViewer.setWidth("400px");
			beanViewer.setHeightFull();
			beanViewer.setDbBean(memberWrapper.get().getMember());
			
			memberLayout.removeAll();
			memberLayout.add(beanViewer);
		}
	}

	@Override
	public void init() {
		if (!isInit) {

			projectsTable.addColumn(Project::getName).setHeader("Projekt");
			membersTable.addColumn(MemberWrapper::getPersonName).setHeader("Mitglied");

			customersTable.addColumn(Customer::getName).setHeader("Name");
			customersTable.addColumn(Customer::getAlias).setHeader("Alias");
			customersTable.addColumn(Customer::getCity).setHeader("Stadt");

			loadCustomers();
			showCustomersLayout(true);
			isInit = true;
		}
	}

	private void showCustomersLayout(boolean show) {
		customersTableLayout.setVisible(true);
		showProjectsLayout(false);		
	}

	private void showProjectsLayout(boolean show) {
		projectsTableLayout.setVisible(show);
		showMembersLayout(false);
	}

	private void showMembersLayout(boolean show) {
		membersTableLayout.setVisible(show);
	}

	private void loadCustomers() {
		Customer customer = new Customer();
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			List<Customer> customers = dbContext.loadObjects(customer, false);
			customersTable.setItems(customers);
			customersTable.setHeightByRows(true);

		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load customers", e);
		}
		
	}

	private void loadProjects(Customer customer) {
		Project project = new Project();
		project.setCustomerId(customer.getId());
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			List<Project> projects = dbContext.loadObjects(project, false);
			projectsTable.setItems(projects);
			projectsTable.setHeightByRows(true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load projects", e);
		}
	}

	private void loadMembers(Project project) {
		Member member= new Member();
		member.setProjectId(project.getId());
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			List<Member> members= dbContext.loadObjects(member, true);
			List<MemberWrapper> memberWrappers = new ArrayList<>();
			for (Member m : members) {
				memberWrappers.add(new MemberWrapper(m));
			}
			membersTable.setItems(memberWrappers);
			membersTable.setHeightByRows(true);
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}		
	}

	
	private void buildMainLayout() {
		setSizeFull();
		
		contentLayout = buildContentLayout();
		add(contentLayout);
	}

	
	private HorizontalLayout buildContentLayout() {
		// common part: create layout
		contentLayout = new HorizontalLayout();
		contentLayout.setSizeFull();
		contentLayout.setMargin(false);
		
		// tablesLayout
		tablesLayout = buildTablesLayout();
		contentLayout.add(tablesLayout);
		
		// customersLayout
		editLayout = buildEditLayout();
		contentLayout.add(editLayout);
		
		return contentLayout;
	}

	
	private VerticalLayout buildTablesLayout() {
		// common part: create layout
		tablesLayout = new VerticalLayout();
		tablesLayout.setHeight("-1px");
		tablesLayout.setWidth("-1px");
		tablesLayout.setMargin(false);
		tablesLayout.setSpacing(true);
		
		// customersTableLayout
		customersTableLayout = buildCustomersTableLayout();
		tablesLayout.add(customersTableLayout);
		
		// projectsTableLayout
		projectsTableLayout = buildProjectsTableLayout();
		tablesLayout.add(projectsTableLayout);
		
		// membersTableLayout
		membersTableLayout = buildMembersTableLayout();
		tablesLayout.add(membersTableLayout);
				
		return tablesLayout;
	}

	
	private VerticalLayout buildEditLayout() {
		// common part: create layout
		editLayout = new VerticalLayout();
		editLayout.setHeightFull();
		editLayout.setWidth("-1px");
		editLayout.setMargin(false);
		editLayout.setSpacing(true);
		
		// customerLayout
		customerLayout = new VerticalLayout();
		customerLayout.setSizeFull();
		customerLayout.setMargin(true);
		editLayout.add(customerLayout);
		
		// projectLayout
		projectLayout = new VerticalLayout();
		projectLayout.setSizeFull();
		projectLayout.setMargin(true);
		editLayout.add(projectLayout);
		
		// memberLayout
		memberLayout = new VerticalLayout();
		memberLayout.setSizeFull();
		memberLayout.setMargin(true);
		editLayout.add(memberLayout);
		
				
		return editLayout;
	}

	
	private HorizontalLayout buildCustomersTableLayout() {
		// common part: create layout
		customersTableLayout = new HorizontalLayout();
		customersTableLayout.setWidth("-1px");
		customersTableLayout.setHeightFull();
		customersTableLayout.setMargin(true);
		customersTableLayout.setSpacing(true);
		
		// customersTable
		// TODO: setLabel
		customersTable = new Grid<Customer>();
//		customersTable.setCaption("Kunden");
		customersTable.setWidth("400px");
		customersTableLayout.add(customersTable);
		
		customerActionsLayout = buildCustomerActionsLayout();
		customersTableLayout.add(customerActionsLayout);
		
		return customersTableLayout;
	}

	
	private VerticalLayout buildCustomerActionsLayout() {
		// common part: create layout
		customerActionsLayout = new VerticalLayout();
		customerActionsLayout.setWidth("-1px");
		customerActionsLayout.setHeight("-1px");
		customerActionsLayout.setMargin(true);
		customerActionsLayout.setSpacing(true);
		
		// editCustomerButton
		editCustomerButton = new Button();
		editCustomerButton.setText("Edit");
		editCustomerButton.setWidth("-1px");
		editCustomerButton.setHeight("-1px");
		customerActionsLayout.add(editCustomerButton);
		
		// newCustomerButton
		newCustomerButton = new Button();
		newCustomerButton.setText("Neu");
		newCustomerButton.setWidth("-1px");
		newCustomerButton.setHeight("-1px");
		customerActionsLayout.add(newCustomerButton);
		
		// deleteCustomerButton
		deleteCustomerButton = new Button();
		deleteCustomerButton.setText("Löschen");
		deleteCustomerButton.setWidth("-1px");
		deleteCustomerButton.setHeight("-1px");
		customerActionsLayout.add(deleteCustomerButton);
		
		return customerActionsLayout;
	}

	
	private HorizontalLayout buildProjectsTableLayout() {
		// common part: create layout
		projectsTableLayout = new HorizontalLayout();
		projectsTableLayout.setWidth("-1px");
		projectsTableLayout.setHeight("-1px");
		projectsTableLayout.setMargin(true);
		projectsTableLayout.setSpacing(true);
		
		// projectsTable
		// TODO: setLabel
		projectsTable = new Grid<Project>();
//		projectsTable.setCaption("Projekte");
		projectsTable.setWidth("400px");
		projectsTable.setHeight("-1px");
		projectsTableLayout.add(projectsTable);
		
		projectActionsLayout = buildProjectActionsLayout();
		projectsTableLayout.add(projectActionsLayout);
		
		return projectsTableLayout;
	}

	
	private VerticalLayout buildProjectActionsLayout() {
		// common part: create layout
		projectActionsLayout = new VerticalLayout();
		projectActionsLayout.setWidth("-1px");
		projectActionsLayout.setHeight("-1px");
		projectActionsLayout.setMargin(true);
		projectActionsLayout.setSpacing(true);
		
		// editProjectButton
		editProjectButton = new Button();
		editProjectButton.setText("Edit");
		editProjectButton.setWidth("-1px");
		editProjectButton.setHeight("-1px");
		projectActionsLayout.add(editProjectButton);
		
		// newProjectButton
		newProjectButton = new Button();
		newProjectButton.setText("Neu");
		newProjectButton.setWidth("-1px");
		newProjectButton.setHeight("-1px");
		projectActionsLayout.add(newProjectButton);
		
		// deleteProjectButton
		deleteProjectButton = new Button();
		deleteProjectButton.setText("Löschen");
		deleteProjectButton.setWidth("-1px");
		deleteProjectButton.setHeight("-1px");
		projectActionsLayout.add(deleteProjectButton);
		
		return projectActionsLayout;
	}

	
	private HorizontalLayout buildMembersTableLayout() {
		// common part: create layout
		membersTableLayout = new HorizontalLayout();
		membersTableLayout.setWidth("-1px");
		membersTableLayout.setHeight("-1px");
		membersTableLayout.setMargin(true);
		membersTableLayout.setSpacing(true);
		
		// membersTable
		// TODO: setLabel
		membersTable = new Grid<MemberWrapper>();
//		membersTable.setCaption("Projektmitglieder");
		membersTable.setWidth("400px");
		membersTable.setHeight("-1px");
		membersTableLayout.add(membersTable);
		
		memberActionsLayout = buildMemberActionsLayout();
		membersTableLayout.add(memberActionsLayout);
		
		return membersTableLayout;
	}

	
	private VerticalLayout buildMemberActionsLayout() {
		// common part: create layout
		memberActionsLayout = new VerticalLayout();
		memberActionsLayout.setWidth("-1px");
		memberActionsLayout.setHeight("-1px");
		memberActionsLayout.setMargin(true);
		memberActionsLayout.setSpacing(true);
		
		// editMemberButton
		editMemberButton = new Button();
		editMemberButton.setText("Edit");
		editMemberButton.setWidth("-1px");
		editMemberButton.setHeight("-1px");
		memberActionsLayout.add(editMemberButton);
		
		// newMemberButton
		newMemberButton = new Button();
		newMemberButton.setText("Neu");
		newMemberButton.setWidth("-1px");
		newMemberButton.setHeight("-1px");
		memberActionsLayout.add(newMemberButton);
		
		// deleteMemberButton
		deleteMemberButton = new Button();
		deleteMemberButton.setText("Löschen");
		deleteMemberButton.setWidth("-1px");
		deleteMemberButton.setHeight("-1px");
		memberActionsLayout.add(deleteMemberButton);
		
		return memberActionsLayout;
	}

}
