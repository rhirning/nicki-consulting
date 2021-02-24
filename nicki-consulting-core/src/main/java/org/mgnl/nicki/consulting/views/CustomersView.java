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
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Grid.SelectionMode;

public class CustomersView extends BaseView implements View {
	
	private Panel mainPanel;
	
	private VerticalLayout mainLayout;
	
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

	private Window editWindow;
	private Window newProjectWindow;
	private Window newMemberWindow;
	private Window selectPersonWindow;

	public CustomersView() {
		buildMainLayout();
		setCompositionRoot(mainPanel);

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
		customerLayout.removeAllComponents();
		customerLayout.addComponent(beanViewer);
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
				UI.getCurrent().removeWindow(editWindow);
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
		editWindow = new Window(windowTitle, beanViewer);
		editWindow.setModal(true);
		editWindow.setHeightFull();
		UI.getCurrent().addWindow(editWindow);
	}

	private void hideProject() {
		projectLayout.removeAllComponents();
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
		
		projectLayout.removeAllComponents();
		projectLayout.addComponent(beanViewer);
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
					UI.getCurrent().removeWindow(newProjectWindow);
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
			newProjectWindow = new Window("Neues Projekt", beanViewer);
			newProjectWindow.setModal(true);
			UI.getCurrent().addWindow(newProjectWindow);
		}
	}

	private void showNewMemberView() {
		PersonSelector selector = new PersonSelector(person -> {
			UI.getCurrent().removeWindow(selectPersonWindow);
			setPerson(person);
			
		});
		selector.init();
		selectPersonWindow = new Window("Person wählen", selector);
		selectPersonWindow.setModal(true);
		UI.getCurrent().addWindow(selectPersonWindow);
	}

	private void setPerson(Person person) {
		Optional<Project> project = getSelectedItem(projectsTable);
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				UI.getCurrent().removeWindow(newMemberWindow);
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
		newMemberWindow = new Window("Neues Projektmitglied", beanViewer);
		newMemberWindow.setModal(true);
		UI.getCurrent().addWindow(newMemberWindow);
	}

	private void hideMember() {
		memberLayout.removeAllComponents();
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
			
			memberLayout.removeAllComponents();
			memberLayout.addComponent(beanViewer);
		}
	}

	@Override
	public void init() {
		if (!isInit) {

			projectsTable.addColumn(Project::getName).setCaption("Projekt");
			membersTable.addColumn(MemberWrapper::getPersonName).setCaption("Mitglied");

			customersTable.addColumn(Customer::getName).setCaption("Name");
			customersTable.addColumn(Customer::getAlias).setCaption("Alias");
			customersTable.addColumn(Customer::getCity).setCaption("Stadt");

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
			if (customers.size() > 0) {
				customersTable.setHeightByRows(customers.size());
			} else {
				customersTable.setHeightByRows(1);
			}

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
			if (projects.size() > 0) {
				projectsTable.setHeightByRows(projects.size());
			} else {
				projectsTable.setHeightByRows(1);
			}
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
			if (memberWrappers.size() > 0) {
				membersTable.setHeightByRows(memberWrappers.size());
			} else {
				membersTable.setHeightByRows(1);
			}
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			LOG.error("Could not load members", e);
		}		
	}

	
	private Panel buildMainLayout() {
		mainPanel = new Panel();
		mainPanel.setSizeFull();
		
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setMargin(false);
		
		// top-level component properties
		setSizeFull();
		
		contentLayout = buildContentLayout();
		mainLayout.addComponent(contentLayout);
		mainPanel.setContent(mainLayout);
		return mainPanel;
	}

	
	private HorizontalLayout buildContentLayout() {
		// common part: create layout
		contentLayout = new HorizontalLayout();
		contentLayout.setSizeFull();
		contentLayout.setMargin(false);
		
		// tablesLayout
		tablesLayout = buildTablesLayout();
		contentLayout.addComponent(tablesLayout);
		
		// customersLayout
		editLayout = buildEditLayout();
		contentLayout.addComponent(editLayout);
		
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
		tablesLayout.addComponent(customersTableLayout);
		
		// projectsTableLayout
		projectsTableLayout = buildProjectsTableLayout();
		tablesLayout.addComponent(projectsTableLayout);
		
		// membersTableLayout
		membersTableLayout = buildMembersTableLayout();
		tablesLayout.addComponent(membersTableLayout);
				
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
		editLayout.addComponent(customerLayout);
		
		// projectLayout
		projectLayout = new VerticalLayout();
		projectLayout.setSizeFull();
		projectLayout.setMargin(true);
		editLayout.addComponent(projectLayout);
		
		// memberLayout
		memberLayout = new VerticalLayout();
		memberLayout.setSizeFull();
		memberLayout.setMargin(true);
		editLayout.addComponent(memberLayout);
		
				
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
		customersTable = new Grid<Customer>();
		customersTable.setCaption("Kunden");
		customersTable.setWidth("400px");
		customersTableLayout.addComponent(customersTable);
		
		customerActionsLayout = buildCustomerActionsLayout();
		customersTableLayout.addComponent(customerActionsLayout);
		
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
		editCustomerButton.setCaption("Edit");
		editCustomerButton.setWidth("-1px");
		editCustomerButton.setHeight("-1px");
		customerActionsLayout.addComponent(editCustomerButton);
		
		// newCustomerButton
		newCustomerButton = new Button();
		newCustomerButton.setCaption("Neu");
		newCustomerButton.setWidth("-1px");
		newCustomerButton.setHeight("-1px");
		customerActionsLayout.addComponent(newCustomerButton);
		
		// deleteCustomerButton
		deleteCustomerButton = new Button();
		deleteCustomerButton.setCaption("Löschen");
		deleteCustomerButton.setWidth("-1px");
		deleteCustomerButton.setHeight("-1px");
		customerActionsLayout.addComponent(deleteCustomerButton);
		
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
		projectsTable = new Grid<Project>();
		projectsTable.setCaption("Projekte");
		projectsTable.setWidth("400px");
		projectsTable.setHeight("-1px");
		projectsTableLayout.addComponent(projectsTable);
		
		projectActionsLayout = buildProjectActionsLayout();
		projectsTableLayout.addComponent(projectActionsLayout);
		
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
		editProjectButton.setCaption("Edit");
		editProjectButton.setWidth("-1px");
		editProjectButton.setHeight("-1px");
		projectActionsLayout.addComponent(editProjectButton);
		
		// newProjectButton
		newProjectButton = new Button();
		newProjectButton.setCaption("Neu");
		newProjectButton.setWidth("-1px");
		newProjectButton.setHeight("-1px");
		projectActionsLayout.addComponent(newProjectButton);
		
		// deleteProjectButton
		deleteProjectButton = new Button();
		deleteProjectButton.setCaption("Löschen");
		deleteProjectButton.setWidth("-1px");
		deleteProjectButton.setHeight("-1px");
		projectActionsLayout.addComponent(deleteProjectButton);
		
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
		membersTable = new Grid<MemberWrapper>();
		membersTable.setCaption("Projektmitglieder");
		membersTable.setWidth("400px");
		membersTable.setHeight("-1px");
		membersTableLayout.addComponent(membersTable);
		
		memberActionsLayout = buildMemberActionsLayout();
		membersTableLayout.addComponent(memberActionsLayout);
		
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
		editMemberButton.setCaption("Edit");
		editMemberButton.setWidth("-1px");
		editMemberButton.setHeight("-1px");
		memberActionsLayout.addComponent(editMemberButton);
		
		// newMemberButton
		newMemberButton = new Button();
		newMemberButton.setCaption("Neu");
		newMemberButton.setWidth("-1px");
		newMemberButton.setHeight("-1px");
		memberActionsLayout.addComponent(newMemberButton);
		
		// deleteMemberButton
		deleteMemberButton = new Button();
		deleteMemberButton.setCaption("Löschen");
		deleteMemberButton.setWidth("-1px");
		deleteMemberButton.setHeight("-1px");
		memberActionsLayout.addComponent(deleteMemberButton);
		
		return memberActionsLayout;
	}

}
