package org.mgnl.nicki.consulting.views;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.TimeHelper;
import org.mgnl.nicki.consulting.core.model.Customer;
import org.mgnl.nicki.consulting.core.model.Member;
import org.mgnl.nicki.consulting.core.model.Project;
import org.mgnl.nicki.consulting.data.MemberWrapper;
import org.mgnl.nicki.core.data.TreeObject;
import org.mgnl.nicki.core.data.TreeObjectWrapper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.profile.InitProfileException;
import org.mgnl.nicki.vaadin.base.application.NickiApplication;
import org.mgnl.nicki.vaadin.base.components.DialogBase;
import org.mgnl.nicki.vaadin.base.menu.application.View;
import org.mgnl.nicki.vaadin.base.notification.Notification;
import org.mgnl.nicki.vaadin.base.notification.Notification.Type;
import org.mgnl.nicki.vaadin.db.editor.DbBeanCloseListener;
import org.mgnl.nicki.vaadin.db.editor.DbBeanViewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class CustomersTreeView extends VerticalLayout implements View {
	
	private SplitLayout horizontalSplitPanel;
	
	private TreeGrid<TreeObject> tree;
	private TreeDataProvider<TreeObject> treeDataProvider;
	private VerticalLayout treeCanvas;
	private VerticalLayout canvas;
	private boolean isInit;
	
	private List<Customer> customers;
	private Map<Customer, Project> projects = new HashMap<>();
	private Map<Project, MemberWrapper> members = new HashMap<>();
	private DialogBase editWindow;


	public CustomersTreeView() {
		buildMainLayout();
		
		init();

	}

	public void init() {
		if (!isInit) {
			
			isInit = true;
		}
		
		treeCanvas.removeAll();
		canvas.removeAll();
		
		tree = new TreeGrid<>();
		tree.setSizeFull();

		collectData();
		tree.setDataProvider(treeDataProvider);
		tree.addHierarchyColumn(TreeObject::getDisplayName);
		tree.addSelectionListener(event -> {
			canvas.removeAll();
			Optional<TreeObject> itemOptional = event.getFirstSelectedItem();
			if (itemOptional.isPresent()) {
				TreeObject target = itemOptional.get().getObject();
				if (target instanceof MemberWrapper) {
					target = ((MemberWrapper) target).getMember();
				}
				showEditView(canvas, target);
			}
		});
		tree.addExpandListener(event -> {
			Collection<TreeObject> items = event.getItems();
			if (items != null && items.size() > 0) {
				tree.select(items.iterator().next());
			}
		});
		tree.addCollapseListener(event -> {
			Collection<TreeObject> items = event.getItems();
			if (items != null && items.size() > 0) {
				tree.select(items.iterator().next());
			}
		});
		// newCustomerButton
		Button newCustomerButton = new Button();
		newCustomerButton.setText("Neuer Kunde");
		newCustomerButton.setWidth("-1px");
		newCustomerButton.setHeight("-1px");
		newCustomerButton.addClickListener(event -> showEditView(Customer.class, Optional.empty(), "Neuer Kunde", "Kunde bearbeiten"));
		treeCanvas.add(newCustomerButton);
		
		treeCanvas.add(tree);
		treeCanvas.setFlexGrow(1, tree);

        GridContextMenu<TreeObject> contextMenu = new GridContextMenu<>(tree);
        // handle item right-click
        contextMenu.setDynamicContentHandler(item -> {
        	contextMenu.removeAll();
            if (item != null) {
            	tree.select(item);
            	TreeObject target = item.getObject();
            	
            	if (target instanceof Customer) {
            		contextMenu.addItem("Löschen", selectedItem -> deleteCustomer(Optional.of((Customer) target)));
            		contextMenu.addItem("Neues Projekt", selectedItem -> showEditView(Project.class, Optional.empty(), "Neues Projekt", "Projekt bearbeiten", target));
            	} else if (target instanceof Project) {
            		contextMenu.addItem("Löschen", selectedItem -> deleteProject(Optional.of((Project) target)));
            		contextMenu.addItem("Neues Projektmitglied", selectedItem -> showEditView(Member.class, Optional.empty(), "Neues Projektmitglied", "Projektmitglied bearbeiten", target));
            	} else if (target instanceof MemberWrapper) {
            		contextMenu.addItem("Löschen", selectedItem -> deleteMember(Optional.of((MemberWrapper) target)));
            	}
            	return true;
            } else {
            	return false;
            }
        });
		
	}

	private <T> void showEditView(VerticalLayout layout, T object) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				init();
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidthFull();
		if (object != null) {
			beanViewer.setDbBean(object);
			layout.add(beanViewer);
		}
	}

	private void deleteCustomer(Optional<Customer> customerOptional ) {
		if (!customerOptional.isPresent()) {
			Notification.show("Bitte erst Kunde auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			Customer customer = customerOptional.get();
			try {
				if (TimeHelper.hasProjects(customer)) {
					Notification.show("Der Kunde besitzt noch Projekte", Type.HUMANIZED_MESSAGE);
				} else {
					TimeHelper.delete(customer);
					init();
				}
			} catch (SQLException | InitProfileException e) {
				log.error("Error accessing db", e);
				Notification.show("Der Kunde konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	private void deleteProject(Optional<Project> projectOptional) {
		if (!projectOptional.isPresent()) {
			Notification.show("Bitte erst Projekt auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			Project project = projectOptional.get();
			try {
				if (TimeHelper.hasMembers(project)) {
					Notification.show("Das Projekt hat noch Projektmitglieder", Type.HUMANIZED_MESSAGE);
				} else {
					TimeHelper.delete(project);
					init();
				}
			} catch (SQLException | InitProfileException e) {
				log.error("Error accessing db", e);
				Notification.show("Das Projekt konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	private void deleteMember(Optional<MemberWrapper> memberWrapperOptional) {
		if (!memberWrapperOptional.isPresent()) {
			Notification.show("Bitte erst Projektmitglied auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			try {
				if (TimeHelper.hasTimeEntries(memberWrapperOptional.get().getMember())) {
					Notification.show("Es gibt Buchungen für diese Projektmitglied", Type.HUMANIZED_MESSAGE);
				} else {
					TimeHelper.delete(memberWrapperOptional.get().getMember());
					init();
				}
			} catch (SQLException | InitProfileException e) {
				log.error("Error accessing db", e);
				Notification.show("Das Projektmitglied konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}
	
	private <T> void showEditView(Class<T> clazz, Optional<T> object, String newCaption, String editCaption, Object... foreignObjects) {
		DbBeanViewer beanViewer = new DbBeanViewer(new DbBeanCloseListener() {
			
			@Override
			public void close(Component component) {
				editWindow.close();
				init();
			}
		});
		beanViewer.setDbContextName("projects");
		beanViewer.setHeightFull();
		beanViewer.setWidth("400px");
		String windowTitle;
		if (!object.isPresent()) {
			try {
				beanViewer.init(clazz, foreignObjects);
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

	private void collectData() {
		this.projects.clear();
		this.members.clear();
		TreeData<TreeObject> treeData = new TreeData<TreeObject>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			customers = loadCustomers(dbContext);
			if (customers != null) {
				customers.stream().forEach(customer -> {
					TreeObjectWrapper<Customer> customerWrapper = new TreeObjectWrapper<Customer>(customer);
					treeData.addItem(null, customerWrapper);
					List<Project> projects;
					try {
						projects = loadProjects(dbContext, customer);
					} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
						log.error("Error reading project for customer: " + customer, e);
						projects = null;
					}
					if (projects != null) {
						projects.stream().forEach(project -> {
							this.projects.put(customer, project);
							TreeObjectWrapper<Project> projectWrapper = new TreeObjectWrapper<Project>(project);
							treeData.addItem(customerWrapper, projectWrapper);
							List<Member> members;
							try {
								members = loadMembers(dbContext, project);
							} catch (InstantiationException | IllegalAccessException | SQLException
									| InitProfileException e) {
								log.error("Error reading members for project: " + project, e);
								members = null;
							}
							if (members != null) {
								members.stream().forEach(member -> {
									MemberWrapper memberWrapper = new MemberWrapper(member);
									this.members.put(project, memberWrapper);
									treeData.addItem(projectWrapper, new TreeObjectWrapper<MemberWrapper>(memberWrapper));
									
								});
							}
						});
					}
				});
			}
			
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customers", e);
		}
		
	}

	private List<Customer> loadCustomers(DBContext dbContext) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		Customer customer = new Customer();
		return dbContext.loadObjects(customer, false);
	}

	private List<Project> loadProjects(DBContext dbContext, Customer customer) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		Project project = new Project();
		project.setCustomerId(customer.getId());
		return dbContext.loadObjects(project, false);
	}

	private List<Member> loadMembers(DBContext dbContext, Project project) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		Member member= new Member();
		member.setProjectId(project.getId());
		return dbContext.loadObjects(member, true);
	}

	
	private void buildMainLayout() {
		setSizeFull();
		setMargin(false);
		setSpacing(false);
		
		// horizontalSplitPanel
		horizontalSplitPanel = new SplitLayout();
		horizontalSplitPanel.setOrientation(Orientation.HORIZONTAL);
		horizontalSplitPanel.setSizeFull();
		add(horizontalSplitPanel);
		
		treeCanvas = new VerticalLayout();
		treeCanvas.setSizeFull();
		treeCanvas.setMargin(false);
		treeCanvas.setSpacing(true);
		horizontalSplitPanel.addToPrimary(treeCanvas);
		
		canvas = new VerticalLayout();
		canvas.setSizeFull();
		canvas.setMargin(false);
		canvas.setSpacing(false);
		horizontalSplitPanel.addToSecondary(canvas);
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

}
