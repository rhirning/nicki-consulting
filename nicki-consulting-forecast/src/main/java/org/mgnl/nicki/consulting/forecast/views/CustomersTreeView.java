package org.mgnl.nicki.consulting.forecast.views;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mgnl.nicki.consulting.core.helper.Constants;
import org.mgnl.nicki.consulting.forecast.helper.ForecastHelper;
import org.mgnl.nicki.consulting.forecast.helper.ForecastViewHelper;
import org.mgnl.nicki.consulting.forecast.model.ForecastCustomer;
import org.mgnl.nicki.consulting.forecast.model.ForecastDeal;
import org.mgnl.nicki.core.data.TreeObject;
import org.mgnl.nicki.core.data.TreeObjectWrapper;
import org.mgnl.nicki.db.context.DBContext;
import org.mgnl.nicki.db.context.DBContextManager;
import org.mgnl.nicki.db.context.NotSupportedException;
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
	
	private List<ForecastCustomer> customers;
	private Map<ForecastCustomer, ForecastDeal> deals = new HashMap<>();
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
				ForecastViewHelper.showEditView(canvas, target, c -> init());
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
		newCustomerButton.addClickListener(event -> showEditView(ForecastCustomer.class, Optional.empty(), "Neuer Kunde", "Kunde bearbeiten"));
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
            	
            	if (target instanceof ForecastCustomer) {
            		contextMenu.addItem("Löschen", selectedItem -> deleteCustomer(Optional.of((ForecastCustomer) target)));
            		contextMenu.addItem("Neuer Deal", selectedItem -> showEditView(ForecastDeal.class, Optional.empty(), "Neuer Deal", "Deal bearbeiten", target));
            	} else if (target instanceof ForecastDeal) {
            		contextMenu.addItem("Löschen", selectedItem -> deleteDeal(Optional.of((ForecastDeal) target)));
            	}
            	return true;
            } else {
            	return false;
            }
        });
		
	}

	private void deleteCustomer(Optional<ForecastCustomer> customerOptional ) {
		if (!customerOptional.isPresent()) {
			Notification.show("Bitte erst Kunde auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			ForecastCustomer customer = customerOptional.get();
			try {
				if (ForecastHelper.hasDeals(customer)) {
					Notification.show("Der Kunde besitzt noch Deals", Type.HUMANIZED_MESSAGE);
				} else {
					ForecastHelper.delete(customer);
					init();
				}
			} catch (SQLException | InitProfileException e) {
				log.error("Error accessing db", e);
				Notification.show("Der Kunde konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
			}
		}
	}

	private void deleteDeal(Optional<ForecastDeal> dealOptional) {
		if (!dealOptional.isPresent()) {
			Notification.show("Bitte erst Deal auswählen", Type.HUMANIZED_MESSAGE);
		} else {
			ForecastDeal deal = dealOptional.get();
			try (DBContext dbContext = DBContextManager.getContext(Constants.DB_CONTEXT_NAME)) {
				deal.setValidTo(new Date());
				dbContext.update(deal, "validTo");
				ForecastHelper.delete(deal);
				init();				
			} catch (SQLException | InitProfileException | NotSupportedException e) {
				log.error("Error accessing db", e);
				Notification.show("Der Deal konnte nicht gelöscht werden", Type.ERROR_MESSAGE);
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
		this.deals.clear();
		TreeData<TreeObject> treeData = new TreeData<TreeObject>();
		treeDataProvider = new TreeDataProvider<>(treeData);
		try (DBContext dbContext = DBContextManager.getContext("projects")) {
			customers = loadCustomers(dbContext);
			if (customers != null) {
				customers.stream().forEach(customer -> {
					TreeObjectWrapper<ForecastCustomer> customerWrapper = new TreeObjectWrapper<ForecastCustomer>(customer);
					treeData.addItem(null, customerWrapper);
					List<ForecastDeal> projects;
					try {
						projects = loadDeals(dbContext, customer);
					} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
						log.error("Error reading project for customer: " + customer, e);
						projects = null;
					}
					if (projects != null) {
						projects.stream().forEach(project -> {
							this.deals.put(customer, project);
							TreeObjectWrapper<ForecastDeal> projectWrapper = new TreeObjectWrapper<ForecastDeal>(project);
							treeData.addItem(customerWrapper, projectWrapper);
						});
					}
				});
			}
			
		} catch (InstantiationException | IllegalAccessException | SQLException | InitProfileException e) {
			log.error("Could not load customers", e);
		}
		
	}

	private List<ForecastCustomer> loadCustomers(DBContext dbContext) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		ForecastCustomer customer = new ForecastCustomer();
		return dbContext.loadObjects(customer, false);
	}

	private List<ForecastDeal> loadDeals(DBContext dbContext, ForecastCustomer customer) throws InstantiationException, IllegalAccessException, SQLException, InitProfileException {
		ForecastDeal deal = new ForecastDeal();
		deal.setCustomerId(customer.getId());
		return dbContext.loadObjects(deal, false);
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
